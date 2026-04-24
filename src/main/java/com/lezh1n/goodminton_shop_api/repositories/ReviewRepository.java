package com.lezh1n.goodminton_shop_api.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    Page<Review> findByProduct_Id(Integer productId, Pageable pageable);

    boolean existsByOrderItem_Id(Integer orderItemId);
}
