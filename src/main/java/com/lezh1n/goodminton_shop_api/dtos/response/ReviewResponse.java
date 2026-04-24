package com.lezh1n.goodminton_shop_api.dtos.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewResponse {
    private Integer reviewId;
    private Integer productId;
    private Integer orderItemId;
    private AccountResponse customer;
    private Short rating;
    private String comment;
    private LocalDateTime createdAt;
}
