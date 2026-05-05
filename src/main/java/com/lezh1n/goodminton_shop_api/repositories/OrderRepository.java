package com.lezh1n.goodminton_shop_api.repositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.Order;
import com.lezh1n.goodminton_shop_api.enums.OrderStatus;
import com.lezh1n.goodminton_shop_api.enums.OrderType;
import com.lezh1n.goodminton_shop_api.enums.PaymentMethod;
import com.lezh1n.goodminton_shop_api.enums.PaymentStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    @EntityGraph(attributePaths = {
            "customer",
            "store",
            "orderItems",
            "orderItems.variant",
            "orderItems.variant.product",
            "orderItems.variant.color",
            "orderItems.variant.size",
            "payments"
    })
    @NonNull
    Optional<Order> findById(@NonNull Integer orderId);

    Page<Order> findByCustomer_Id(Integer customerId, Pageable pageable);

    Page<Order> findByStore_Id(Integer storeId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByOrderTypeAndStatus(OrderType orderType, OrderStatus status, Pageable pageable);

    // ---------- Dashboard aggregates ----------

    @Query("""
            SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o
            WHERE o.status = :status AND o.orderDate BETWEEN :from AND :to
            """)
    BigDecimal sumRevenue(@Param("status") OrderStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("""
            SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o
            WHERE o.store.id = :storeId
              AND o.status = :status
              AND o.orderDate BETWEEN :from AND :to
            """)
    BigDecimal sumRevenueByStore(@Param("storeId") Integer storeId,
            @Param("status") OrderStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    long countByStatusAndOrderDateBetween(OrderStatus status, LocalDateTime from, LocalDateTime to);

    long countByStatusAndOrderTypeAndOrderDateBetween(OrderStatus status, OrderType type,
            LocalDateTime from, LocalDateTime to);

    long countByStore_IdAndStatusAndOrderDateBetween(Integer storeId, OrderStatus status,
            LocalDateTime from, LocalDateTime to);

    long countByStore_IdAndStatusAndOrderTypeAndOrderDateBetween(Integer storeId, OrderStatus status,
            OrderType type, LocalDateTime from, LocalDateTime to);

    // Returns rows: [Date date, BigDecimal revenue, Long orderCount]
    @Query(value = """
            SELECT CAST(o.order_date AS DATE) AS day,
                   SUM(o.total_amount) AS revenue,
                   COUNT(*) AS order_count
            FROM orders o
            WHERE o.status = CAST(:status AS order_status)
              AND o.order_date BETWEEN :from AND :to
              AND (:storeId IS NULL OR o.store_id = :storeId)
            GROUP BY CAST(o.order_date AS DATE)
            ORDER BY day ASC
            """, nativeQuery = true)
    List<Object[]> findDailyRevenueRaw(@Param("status") String status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("storeId") Integer storeId);

    // Returns rows: [Integer storeId, String storeName, BigDecimal revenue, Long
    // orderCount]
    @Query("""
            SELECT s.id, s.name, COALESCE(SUM(o.totalAmount), 0), COUNT(o)
            FROM Order o JOIN o.store s
            WHERE o.status = :status AND o.orderDate BETWEEN :from AND :to
            GROUP BY s.id, s.name
            ORDER BY SUM(o.totalAmount) DESC
            """)
    List<Object[]> findRevenueByStoreRaw(@Param("status") OrderStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    // ---------- Scheduled job queries ----------

    /**
     * Online orders DELIVERED with order date older than threshold — eligible for
     * auto-complete.
     */
    @Query("""
            SELECT o FROM Order o
            WHERE o.status = :status
              AND o.orderType = :type
              AND o.orderDate < :threshold
            """)
    List<Order> findEligibleForAutoComplete(@Param("status") OrderStatus status,
            @Param("type") OrderType type,
            @Param("threshold") LocalDateTime threshold);

    /**
     * PENDING orders with VNPay payment older than threshold and no PAID payment —
     * expired.
     */
    @EntityGraph(attributePaths = { "orderItems", "orderItems.variant", "store", "payments" })
    @Query("""
            SELECT DISTINCT o FROM Order o
            JOIN o.payments p
            WHERE o.status = :pendingStatus
              AND p.method = :vnpayMethod
              AND p.status = :pendingPaymentStatus
              AND p.createdAt < :threshold
              AND NOT EXISTS (
                  SELECT 1 FROM Payment p2
                  WHERE p2.order = o AND p2.status = :paidPaymentStatus
              )
            """)
    List<Order> findExpiredVNPayPending(
            @Param("pendingStatus") OrderStatus pendingStatus,
            @Param("vnpayMethod") PaymentMethod vnpayMethod,
            @Param("pendingPaymentStatus") PaymentStatus pendingPaymentStatus,
            @Param("paidPaymentStatus") PaymentStatus paidPaymentStatus,
            @Param("threshold") LocalDateTime threshold);
}
