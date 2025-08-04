package com.lezh1n.goodminton_shop_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

}
