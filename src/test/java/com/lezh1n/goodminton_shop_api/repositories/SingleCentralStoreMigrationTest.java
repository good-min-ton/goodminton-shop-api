package com.lezh1n.goodminton_shop_api.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import jakarta.persistence.EntityManager;

/**
 * V11 makes "at most one central store" a database rule.
 *
 * <p>It was previously enforced only by StoreServiceImpl.demoteCurrentCentral,
 * while InventoryService.findCentralStore() reads it through Optional&lt;Store&gt;.
 * A second central row would therefore surface as an
 * IncorrectResultSizeDataAccessException on every ONLINE order - far from the
 * write that caused it, and long after.
 *
 * <p>Seeds its own account: DataInitializer is an application component and does
 * not run inside a @DataJpaTest slice.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SingleCentralStoreMigrationTest {

    @Autowired
    TestEntityManager em;

    private Integer adminId;
    private long stamp;

    private EntityManager entityManager() {
        return em.getEntityManager();
    }

    @BeforeEach
    void seedAdmin() {
        stamp = System.nanoTime();
        entityManager().createNativeQuery(
                "INSERT INTO accounts (full_name, phone, email, password, role) "
                        + "VALUES ('Test Admin', ?1, ?2, 'x', 'SUPER_ADMIN'::user_role)")
                .setParameter(1, "09" + (stamp % 100000000L))
                .setParameter(2, "admin" + stamp + "@example.com")
                .executeUpdate();
        entityManager().flush();
        adminId = ((Number) entityManager()
                .createNativeQuery("SELECT id FROM accounts WHERE email = ?1")
                .setParameter(1, "admin" + stamp + "@example.com")
                .getSingleResult()).intValue();
    }

    /** Inserts a store row directly, bypassing the service-layer demote logic. */
    private void insertStore(String name, boolean central) {
        entityManager().createNativeQuery(
                "INSERT INTO stores (admin_id, name, address, contact, longitude, latitude, is_central) "
                        + "VALUES (?1, ?2, 'addr', '0900000000', 106.7, 10.8, ?3)")
                .setParameter(1, adminId)
                .setParameter(2, name)
                .setParameter(3, central)
                .executeUpdate();
        entityManager().flush();
    }

    @Test
    void theIndexExistsAfterMigration() {
        Object count = entityManager().createNativeQuery(
                "SELECT COUNT(*) FROM pg_indexes WHERE indexname = 'uq_stores_single_central'")
                .getSingleResult();

        assertThat(((Number) count).intValue()).isEqualTo(1);
    }

    @Test
    void aSecondCentralStoreIsRejected() {
        // Demote rather than delete: a seeded central store is referenced by
        // inventory rows, so removing it trips fk_inventory_store.
        entityManager().createNativeQuery("UPDATE stores SET is_central = false WHERE is_central")
                .executeUpdate();
        insertStore("Central " + stamp, true);

        assertThatThrownBy(() -> insertStore("Second central " + stamp, true))
                .hasStackTraceContaining("uq_stores_single_central");
    }

    @Test
    void anyNumberOfNonCentralStoresIsAllowed() {
        insertStore("Branch A " + stamp, false);
        insertStore("Branch B " + stamp, false);
        insertStore("Branch C " + stamp, false);

        Object count = entityManager().createNativeQuery(
                "SELECT COUNT(*) FROM stores WHERE is_central = false")
                .getSingleResult();

        assertThat(((Number) count).longValue()).isGreaterThanOrEqualTo(3);
    }
}
