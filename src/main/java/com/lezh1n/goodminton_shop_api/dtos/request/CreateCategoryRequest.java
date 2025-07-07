package com.lezh1n.goodminton_shop_api.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateCategoryRequest {

    @NotBlank(message = "CATEGORY_NAME_REQUIRED")
    private String name;
    
    @NotBlank(message = "CATEGORY_DESCRIPTION_REQUIRED")
    private String description;
}
