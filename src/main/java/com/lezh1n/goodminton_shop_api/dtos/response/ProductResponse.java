package com.lezh1n.goodminton_shop_api.dtos.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {
    private Integer id;
    private CategoryResponse category;
    private BrandResponse brand;
    private Integer relatedProductId;
    private String name;
    private String description;
    private String slug;
    private Boolean isVisible;
    private ResourceResponse thumbnail;
    private LocalDateTime createdAt;
    private List<ProductSpecificationResponse> specifications;
    private List<ProductVariantResponse> variants;
}
