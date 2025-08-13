package com.lezh1n.goodminton_shop_api.mappers;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.request.ReviewRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.ReviewResponse;
import com.lezh1n.goodminton_shop_api.entities.Product;
import com.lezh1n.goodminton_shop_api.entities.Review;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewMapper {

    private final AccountMapper accountMapper;

    public Review toReview(Product product, ReviewRequest request) {
        return Review.builder()
                .product(product)
                .rating(request.getRating())
                .comment(request.getComment())
                .createAt(LocalDateTime.now())
                .build();
    }

    public ReviewResponse toReviewResponse(Review review) {
        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .productId(review.getProduct().getProductId())
                .user(accountMapper.toAccountResponse(review.getUser()))
                .rating(review.getRating())
                .comment(review.getComment())
                .createAt(review.getCreateAt())
                .build();
    }
}
