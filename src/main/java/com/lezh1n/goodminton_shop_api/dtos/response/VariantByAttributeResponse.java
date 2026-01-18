package com.lezh1n.goodminton_shop_api.dtos.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VariantByAttributeResponse {
    private Integer productId;
    private String name;
    private String description;
    private String thumbnailUrl;
    private LocalDateTime createAt;
    private SpecificVariantResponse variant;
}
