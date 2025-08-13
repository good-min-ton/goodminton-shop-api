package com.lezh1n.goodminton_shop_api.dtos.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ReviewRequest {
    @Min(value = 1, message = "REVIEW_RATING_OUT_OF_RANGE")
    @Max(value = 5, message = "REVIEW_RATING_OUT_OF_RANGE")
    @NotBlank(message = "REVIEW_RATING_BLANK")
    private Integer rating;
    private String comment;
}
