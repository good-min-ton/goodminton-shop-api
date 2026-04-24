package com.lezh1n.goodminton_shop_api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.Order;
import com.lezh1n.goodminton_shop_api.enums.OrderStatus;
import com.lezh1n.goodminton_shop_api.enums.OrderType;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByOrderTypeAndStatusIn(OrderType orderType, List<OrderStatus> statuses);

    List<Order> findByCustomer_Id(Integer customerId);

    List<Order> findByStore_Id(Integer storeId);
}
