package com.lezh1n.goodminton_shop_api.dtos.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ReviewRequest {

    @NotNull(message = "REVIEW_ORDER_ITEM_BLANK")
    private Integer orderItemId;

    @Min(value = 1, message = "REVIEW_RATING_OUT_OF_RANGE")
    @Max(value = 5, message = "REVIEW_RATING_OUT_OF_RANGE")
    @NotNull(message = "REVIEW_RATING_BLANK")
    private Short rating;

    private String comment;
}
