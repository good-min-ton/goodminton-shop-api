package com.lezh1n.goodminton_shop_api.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import jakarta.persistence.EntityManager;

/**
 * A customer calls about an order and quotes whatever they have: a tracking
 * code, the phone number they gave, or the order number. None of the three was
 * searchable before, so the call had nowhere to go.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderSearchTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    OrderRepository orderRepository;

    private Integer storeA;
    private Integer storeB;
    private Integer orderInA;
    private Integer orderInB;
    private long stamp;

    private EntityManager entityManager() {
        return em.getEntityManager();
    }

    private Integer insertStore(String name) {
        Integer adminId = ((Number) entityManager()
                .createNativeQuery("SELECT id FROM accounts ORDER BY id LIMIT 1")
                .getSingleResult()).intValue();
        entityManager().createNativeQuery(
                "INSERT INTO stores (admin_id, name, address, contact, longitude, latitude, is_central) "
                        + "VALUES (?1, ?2, 'a', '09', 106.7, 10.8, false)")
                .setParameter(1, adminId).setParameter(2, name).executeUpdate();
        entityManager().flush();
        return ((Number) entityManager()
                .createNativeQuery("SELECT id FROM stores WHERE name = ?1")
                .setParameter(1, name).getSingleResult()).intValue();
    }

    private Integer insertOrder(Integer storeId, String shippingCode, String phone) {
        entityManager().createNativeQuery(
                "INSERT INTO orders (customer_id, store_id, order_type, status, total_amount, "
                        + "shipping_code, recipient_phone, order_date, status_changed_at) "
                        + "VALUES (NULL, ?1, 'ONLINE'::order_type, 'SHIPPING'::order_status, 100000, "
                        + "?2, ?3, NOW(), NOW())")
                .setParameter(1, storeId).setParameter(2, shippingCode)
                .setParameter(3, phone).executeUpdate();
        entityManager().flush();
        return ((Number) entityManager()
                .createNativeQuery("SELECT id FROM orders WHERE shipping_code = ?1")
                .setParameter(1, shippingCode).getSingleResult()).intValue();
    }

    @BeforeEach
    void seed() {
        stamp = System.nanoTime();
        // An account must exist for stores to reference; DataInitializer does not
        // run in a @DataJpaTest slice.
        entityManager().createNativeQuery(
                "INSERT INTO accounts (full_name, phone, email, password, role) "
                        + "VALUES ('T', ?1, ?2, 'x', 'SUPER_ADMIN'::user_role)")
                .setParameter(1, "09" + (stamp % 100000000L))
                .setParameter(2, "t" + stamp + "@e.com")
                .executeUpdate();
        entityManager().flush();

        storeA = insertStore("A " + stamp);
        storeB = insertStore("B " + stamp);
        orderInA = insertOrder(storeA, "GHN" + stamp, "0912345678");
        orderInB = insertOrder(storeB, "GHTK" + stamp, "0987654321");
    }

    private java.util.List<Integer> search(String q, Integer orderId, Integer storeId) {
        return orderRepository.search(q, orderId, storeId, PageRequest.of(0, 20))
                .map(o -> o.getId()).getContent();
    }

    @Test
    @DisplayName("a tracking code finds its order")
    void findsByShippingCode() {
        assertThat(search("GHN" + stamp, null, null)).containsExactly(orderInA);
    }

    @Test
    @DisplayName("a tracking code typed in the wrong case still finds it")
    void shippingCodeIsCaseInsensitive() {
        // A customer reading a code back over the phone types it however it sounds.
        assertThat(search(("ghn" + stamp).toLowerCase(), null, null)).containsExactly(orderInA);
    }

    @Test
    @DisplayName("a recipient phone number finds its order")
    void findsByPhone() {
        assertThat(search("0912345678", null, null)).containsExactly(orderInA);
    }

    @Test
    @DisplayName("an order number finds its order")
    void findsByOrderId() {
        assertThat(search(String.valueOf(orderInA), orderInA, null)).contains(orderInA);
    }

    @Test
    @DisplayName("a partial tracking code matches nothing")
    void partialCodeDoesNotMatch() {
        // Deliberate: a fragment would return half the day's dispatches, which
        // is worse than telling the caller it was not found.
        assertThat(search("GHN", null, null)).isEmpty();
    }

    @Test
    @DisplayName("a store admin cannot reach another branch's order")
    void searchIsScopedByStore() {
        assertThat(search("GHTK" + stamp, null, storeA)).isEmpty();
        assertThat(search("GHTK" + stamp, null, storeB)).containsExactly(orderInB);
    }

    @Test
    @DisplayName("an unrestricted search spans every store")
    void nullStoreSearchesEverything() {
        assertThat(search("GHTK" + stamp, null, null)).containsExactly(orderInB);
    }

    @Test
    @DisplayName("a phone number is not mistaken for an order id")
    void phoneIsNotAnOrderId() {
        // 0912345678 exceeds Integer range; treating the term as an id would
        // throw before the phone match ever ran.
        assertThat(search("0912345678", null, null)).containsExactly(orderInA);
    }

    @Test
    @DisplayName("an order with no tracking code yet is not matched by an empty one")
    void nullShippingCodeIsNotMatched() {
        entityManager().createNativeQuery(
                "INSERT INTO orders (customer_id, store_id, order_type, status, total_amount, "
                        + "shipping_code, recipient_phone, order_date, status_changed_at) "
                        + "VALUES (NULL, ?1, 'ONLINE'::order_type, 'PENDING'::order_status, 1000, "
                        + "NULL, NULL, NOW(), NOW())")
                .setParameter(1, storeA).executeUpdate();
        entityManager().flush();

        assertThat(search("", null, null)).isEmpty();
    }

    @Test
    @DisplayName("status_changed_at comes back so a queue can show the wait")
    void statusChangedAtIsPersisted() {
        LocalDateTime changed = orderRepository.findById(orderInA).orElseThrow().getStatusChangedAt();

        assertThat(changed).isNotNull();
    }
}
