package com.lezh1n.goodminton_shop_api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.ProductSpecification;

@Repository
public interface ProductSpecificationRepository extends JpaRepository<ProductSpecification, Integer> {

    List<ProductSpecification> findByProduct_Id(Integer productId);

    void deleteByProduct_Id(Integer productId);
}
