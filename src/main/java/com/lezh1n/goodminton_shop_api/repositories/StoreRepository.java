package com.lezh1n.goodminton_shop_api.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(value = """
            SELECT * FROM stores s
            WHERE immutable_unaccent(lower(s.name || ' ' || s.address))
                  % immutable_unaccent(lower(:query))
               OR immutable_unaccent(lower(s.name)) LIKE '%' || immutable_unaccent(lower(:query)) || '%'
               OR immutable_unaccent(lower(s.address)) LIKE '%' || immutable_unaccent(lower(:query)) || '%'
            ORDER BY similarity(
                immutable_unaccent(lower(s.name || ' ' || s.address)),
                immutable_unaccent(lower(:query))
            ) DESC,
            s.name
            """, countQuery = """
            SELECT COUNT(*) FROM stores s
            WHERE immutable_unaccent(lower(s.name || ' ' || s.address))
                  % immutable_unaccent(lower(:query))
               OR immutable_unaccent(lower(s.name)) LIKE '%' || immutable_unaccent(lower(:query)) || '%'
               OR immutable_unaccent(lower(s.address)) LIKE '%' || immutable_unaccent(lower(:query)) || '%'
            """, nativeQuery = true)
    Page<Store> searchStores(@Param("query") String query, Pageable pageable);
}
