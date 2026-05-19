package com.lezh1n.goodminton_shop_api.repositories;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.OrderItem;
import com.lezh1n.goodminton_shop_api.enums.OrderStatus;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    boolean existsByVariant_Id(Integer variantId);

    @Query("""
            SELECT oi.variant.product.id FROM OrderItem oi
            WHERE oi.order.status = :status
              AND oi.order.orderDate >= :since
              AND oi.variant.product.isVisible = true
              AND oi.variant.product.id NOT IN :excluded
            GROUP BY oi.variant.product.id
            ORDER BY SUM(oi.quantity) DESC
            """)
    List<Integer> findBestSellerProductIds(
            @Param("status") OrderStatus status,
            @Param("since") LocalDateTime since,
            @Param("excluded") Collection<Integer> excluded,
            Pageable pageable);
}
