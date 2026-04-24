package com.lezh1n.goodminton_shop_api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.Inventory;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    boolean existsByStore_Id(Integer storeId);

    List<Inventory> findByStore_Id(Integer storeId);

    Optional<Inventory> findByStore_IdAndVariant_Id(Integer storeId, Integer variantId);

    Integer countByVariant_Id(Integer variantId);
}
