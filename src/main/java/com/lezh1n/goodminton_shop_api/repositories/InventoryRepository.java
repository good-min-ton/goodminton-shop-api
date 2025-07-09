package com.lezh1n.goodminton_shop_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.Inventory;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    @Query(value = "SELECT EXISTS(SELECT 1 FROM inventory WHERE store_id = :storeId)", nativeQuery = true)
    boolean existsByStoreId(@Param("storeId") Integer storeId);
}
