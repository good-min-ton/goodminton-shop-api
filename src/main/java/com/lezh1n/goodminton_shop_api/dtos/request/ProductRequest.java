package com.lezh1n.goodminton_shop_api.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ProductRequest {

    @NotNull(message = "PRODUCT_CATEGORY_BLANK")
    private Integer categoryId;

    @NotNull(message = "PRODUCT_BRAND_BLANK")
    private Integer brandId;

    @NotBlank(message = "PRODUCT_NAME_BLANK")
    private String name;

    private String description;

    @NotBlank(message = "PRODUCT_THUMBNAIL_BLANK")
    private String thumbnailUrl;
}
