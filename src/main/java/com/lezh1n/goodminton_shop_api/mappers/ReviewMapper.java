package com.lezh1n.goodminton_shop_api.mappers;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.request.ReviewRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.ReviewResponse;
import com.lezh1n.goodminton_shop_api.entities.Account;
import com.lezh1n.goodminton_shop_api.entities.OrderItem;
import com.lezh1n.goodminton_shop_api.entities.Product;
import com.lezh1n.goodminton_shop_api.entities.Review;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewMapper {

    private final AccountMapper accountMapper;

    public Review toReview(Product product, Account customer, OrderItem orderItem, ReviewRequest request) {
        return Review.builder()
                .product(product)
                .customer(customer)
                .orderItem(orderItem)
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public ReviewResponse toReviewResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .orderItemId(review.getOrderItem().getId())
                .customer(accountMapper.toAccountResponse(review.getCustomer()))
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
