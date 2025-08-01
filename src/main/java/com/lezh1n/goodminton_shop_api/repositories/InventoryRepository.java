package com.lezh1n.goodminton_shop_api.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.Inventory;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    @Query(value = "SELECT EXISTS(SELECT 1 FROM inventory WHERE store_id = :storeId)", nativeQuery = true)
    boolean existsByStoreId(@Param("storeId") Integer storeId);

    @Query("""
            SELECT i from Inventory i
            WHERE i.variantSize.variantSizeId =:variantSizeId
            AND i.store.storeId = :storeId
            """)
    Optional<Inventory> findByVariantAndStore(
            @Param("variantSizeId") Integer variantSizeId,
            @Param("storeId") Integer storeId);

    @Query("""
            SELECT SUM(i.quantity) FROM Inventory i
            WHERE i.variantSize.variantSizeId = :variantSizeId
            """)
    Integer sumQuantityByVariantSize(@Param("variantSizeId") Integer variantSizeId);

    boolean existsByVariantSizeVariantSizeId(Integer variantSizeId);
}
