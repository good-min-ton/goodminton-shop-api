package com.lezh1n.goodminton_shop_api.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.Inventory;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    boolean existsByStore_Id(Integer storeId);

    List<Inventory> findByStore_Id(Integer storeId);

    Page<Inventory> findByStore_Id(Integer storeId, Pageable pageable);

    Optional<Inventory> findByStore_IdAndVariant_Id(Integer storeId, Integer variantId);

    boolean existsByVariant_Id(Integer variantId);

    // Atomic decrement — success if quantity is enough
    @Modifying
    @Query("""
            UPDATE Inventory i
            SET i.quantity = i.quantity - :qty, i.updatedAt = :now
            WHERE i.store.id = :storeId
              AND i.variant.id = :variantId
              AND i.quantity >= :qty
            """)
    int decrementIfAvailable(@Param("storeId") Integer storeId,
            @Param("variantId") Integer variantId,
            @Param("qty") int qty,
            @Param("now") LocalDateTime now);

    @Modifying
    @Query("""
            UPDATE Inventory i
            SET i.quantity = i.quantity + :qty, i.updatedAt = :now
            WHERE i.store.id = :storeId AND i.variant.id = :variantId
            """)
    int increment(@Param("storeId") Integer storeId,
            @Param("variantId") Integer variantId,
            @Param("qty") int qty,
            @Param("now") LocalDateTime now);
}
