package com.lezh1n.goodminton_shop_api.services;

import org.springframework.data.domain.Page;

import com.lezh1n.goodminton_shop_api.dtos.request.ReviewRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.ReviewResponse;

public interface ReviewService {

    ReviewResponse createReview(Integer productId, ReviewRequest request);

    Page<ReviewResponse> getReviews(Integer productId, int page, int size, String sortBy,
            String sortDir);
}
