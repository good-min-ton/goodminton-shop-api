package com.lezh1n.goodminton_shop_api.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    boolean existsByCategory_Id(Integer categoryId);

    boolean existsByBrand_Id(Integer brandId);

    boolean existsBySlug(String slug);

    boolean existsByRelatedProduct_Id(Integer relatedProductId);

    @EntityGraph(attributePaths = {
            "category",
            "brand",
            "relatedProduct",
            "specifications",
            "variants",
            "variants.color",
            "variants.size"
    })
    @NonNull
    Optional<Product> findById(@NonNull Integer productId);
}
