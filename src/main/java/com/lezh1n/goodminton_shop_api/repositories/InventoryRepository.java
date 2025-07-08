package com.lezh1n.goodminton_shop_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lezh1n.goodminton_shop_api.entities.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

}
