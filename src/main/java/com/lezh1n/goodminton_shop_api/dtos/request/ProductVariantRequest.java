package com.lezh1n.goodminton_shop_api.dtos.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ProductVariantRequest {

    @NotNull(message = "VARIANT_VERSION_BLANK")
    private Integer versionId;

    @NotNull(message = "VARIANT_COLOR_BLANK")
    private Integer colorId;

    @NotEmpty(message = "VARIANT_SIZE_BLANK")
    private List<VariantSizeRequest> sizes;

    private List<String> images;
}
