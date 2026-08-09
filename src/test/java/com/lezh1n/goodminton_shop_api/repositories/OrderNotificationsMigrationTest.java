package com.lezh1n.goodminton_shop_api.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import jakarta.persistence.EntityManager;

/**
 * V12 gives the order lifecycle the three things it needed to stop losing
 * orders: somewhere to record a notification, a timestamp saying how long an
 * order has sat where it is, and indexes to find one a customer is calling
 * about.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderNotificationsMigrationTest {

    @Autowired
    TestEntityManager em;

    private EntityManager entityManager() {
        return em.getEntityManager();
    }

    private long count(String sql, Object... params) {
        var query = entityManager().createNativeQuery(sql);
        for (int i = 0; i < params.length; i++) {
            query.setParameter(i + 1, params[i]);
        }
        return ((Number) query.getSingleResult()).longValue();
    }

    @Test
    @DisplayName("the notifications table exists with the columns the service writes")
    void notificationsTableExists() {
        assertThat(count(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'notifications'"))
                .isGreaterThan(0);

        for (String column : new String[] {
                "recipient_id", "order_id", "type", "message", "read_at", "created_at" }) {
            assertThat(count(
                    "SELECT COUNT(*) FROM information_schema.columns "
                            + "WHERE table_name = 'notifications' AND column_name = ?1",
                    column))
                    .as("notifications.%s", column)
                    .isEqualTo(1);
        }
    }

    @Test
    @DisplayName("the unread badge has a partial index so it does not scan read history")
    void unreadIndexIsPartial() {
        Object def = entityManager().createNativeQuery(
                "SELECT indexdef FROM pg_indexes WHERE indexname = 'idx_notifications_unread'")
                .getSingleResult();

        assertThat(def.toString()).contains("read_at IS NULL");
    }

    @Test
    @DisplayName("orders gain status_changed_at, backfilled rather than left null")
    void statusChangedAtIsBackfilled() {
        assertThat(count(
                "SELECT COUNT(*) FROM information_schema.columns "
                        + "WHERE table_name = 'orders' AND column_name = 'status_changed_at' "
                        + "AND is_nullable = 'NO'"))
                .isEqualTo(1);

        // Every existing row got order_date, never null: a null here would make
        // "how long has this been stuck" unanswerable for exactly the old orders
        // most likely to be stuck.
        assertThat(count("SELECT COUNT(*) FROM orders WHERE status_changed_at IS NULL"))
                .isZero();
    }

    @Test
    @DisplayName("an order can be looked up by the tracking code a customer quotes")
    void shippingCodeIsIndexed() {
        assertThat(count(
                "SELECT COUNT(*) FROM pg_indexes WHERE indexname = 'idx_orders_shipping_code'"))
                .isEqualTo(1);
        assertThat(count(
                "SELECT COUNT(*) FROM pg_indexes WHERE indexname = 'idx_orders_recipient_phone'"))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("the attention queue can order a status by how long it has waited")
    void statusQueueIsIndexed() {
        assertThat(count(
                "SELECT COUNT(*) FROM pg_indexes WHERE indexname = 'idx_orders_status_changed'"))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("a notification is removed with the order it belongs to")
    void notificationsCascadeWithTheirOrder() {
        // ON DELETE CASCADE, not RESTRICT: a notification pointing at a deleted
        // order would render as a dead link in the bell.
        Object rule = entityManager().createNativeQuery(
                "SELECT rc.delete_rule FROM information_schema.referential_constraints rc "
                        + "WHERE rc.constraint_name = 'fk_notifications_order'")
                .getSingleResult();

        assertThat(rule.toString()).isEqualTo("CASCADE");
    }
}
