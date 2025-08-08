package com.lezh1n.goodminton_shop_api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.Order;
import com.lezh1n.goodminton_shop_api.enums.OrderStatus;
import com.lezh1n.goodminton_shop_api.enums.OrderType;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    @EntityGraph(attributePaths = { "customer", "orderItems.variantSize.variant.product" })
    @NonNull
    Optional<Order> findById(@NonNull Integer orderId);

    @EntityGraph(attributePaths = { "customer", "orderItems.variantSize.variant.product" })
    List<Order> findByOrderStatus(OrderStatus status);

    @EntityGraph(attributePaths = { "customer", "orderItems.variantSize.variant.product" })
    List<Order> findByOrderTypeAndOrderStatusIn(OrderType orderType, List<OrderStatus> statuses);
}
