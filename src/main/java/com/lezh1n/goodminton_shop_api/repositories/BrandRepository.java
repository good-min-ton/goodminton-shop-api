package com.lezh1n.goodminton_shop_api.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.Brand;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Integer> {

    @Query(value = """
            SELECT * FROM brands b
            WHERE immutable_unaccent(lower(b.name)) % immutable_unaccent(lower(:query))
               OR immutable_unaccent(lower(b.name)) LIKE '%' || immutable_unaccent(lower(:query)) || '%'
            ORDER BY similarity(immutable_unaccent(lower(b.name)), immutable_unaccent(lower(:query))) DESC,
                     b.name
            """,
            countQuery = """
            SELECT COUNT(*) FROM brands b
            WHERE immutable_unaccent(lower(b.name)) % immutable_unaccent(lower(:query))
               OR immutable_unaccent(lower(b.name)) LIKE '%' || immutable_unaccent(lower(:query)) || '%'
            """,
            nativeQuery = true)
    Page<Brand> searchBrands(@Param("query") String query, Pageable pageable);
}
