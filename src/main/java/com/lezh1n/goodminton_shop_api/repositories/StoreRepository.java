package com.lezh1n.goodminton_shop_api.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.Store;

@Repository
public interface StoreRepository extends JpaRepository<Store, Integer> {

    @Query(value = "SELECT EXISTS (SELECT 1 FROM stores WHERE admin_id = :id)", nativeQuery = true)
    boolean isAdminAssigned(@Param("id") Integer id);

    Optional<Store> findByIsCentralTrue();

    Optional<Store> findByAdmin_Id(Integer adminId);
}
