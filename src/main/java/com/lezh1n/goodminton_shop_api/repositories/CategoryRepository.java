package com.lezh1n.goodminton_shop_api.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    @Query(value = """
            SELECT * FROM categories c
            WHERE immutable_unaccent(lower(c.name)) % immutable_unaccent(lower(:query))
               OR immutable_unaccent(lower(c.name)) LIKE '%' || immutable_unaccent(lower(:query)) || '%'
            ORDER BY similarity(immutable_unaccent(lower(c.name)), immutable_unaccent(lower(:query))) DESC,
                     c.name
            """, countQuery = """
            SELECT COUNT(*) FROM categories c
            WHERE immutable_unaccent(lower(c.name)) % immutable_unaccent(lower(:query))
               OR immutable_unaccent(lower(c.name)) LIKE '%' || immutable_unaccent(lower(:query)) || '%'
            """, nativeQuery = true)
    Page<Category> searchCategories(@Param("query") String query, Pageable pageable);
}
