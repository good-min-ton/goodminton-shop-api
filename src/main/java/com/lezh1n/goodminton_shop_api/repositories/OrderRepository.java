package com.lezh1n.goodminton_shop_api.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.Order;
import com.lezh1n.goodminton_shop_api.enums.OrderStatus;
import com.lezh1n.goodminton_shop_api.enums.OrderType;

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
}
