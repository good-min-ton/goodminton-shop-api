package com.lezh1n.goodminton_shop_api.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class VariantImageRequest {

    @NotBlank(message = "VARIANT_IMAGE_URL_REQUIRED")
    private String imageUrl;

    private String publicId;

    private Integer sortOrder = 0;
}
