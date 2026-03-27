package com.lezh1n.goodminton_shop_api.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.OrderItemAllocation;

@Repository
public interface InventoryAllocationRepository extends JpaRepository<OrderItemAllocation, Integer> {

    List<OrderItemAllocation> findByOrderItemOrderItemId(Integer orderItemId);
}
