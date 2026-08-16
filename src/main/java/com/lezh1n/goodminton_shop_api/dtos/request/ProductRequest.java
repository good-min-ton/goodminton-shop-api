package com.lezh1n.goodminton_shop_api.dtos.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ProductRequest {

    @NotNull(message = "PRODUCT_CATEGORY_BLANK")
    private Integer categoryId;

    @NotNull(message = "PRODUCT_BRAND_BLANK")
    private Integer brandId;

    private Integer relatedProductId;

    @NotBlank(message = "PRODUCT_NAME_BLANK")
    private String name;

    private String description;

    @NotBlank(message = "PRODUCT_SLUG_BLANK")
    private String slug;

    private Boolean isVisible;

    // @Valid is what makes Bean Validation descend into the list elements.
    // Without it the constraints declared inside ProductSpecificationRequest and
    // ProductVariantRequest are never evaluated - SPEC_NAME_REQUIRED,
    // VARIANT_SKU_BLANK, VARIANT_PRICE_BLANK and VARIANT_PRICE_MUST_BE_POSITIVE
    // existed only at their declaration site and could not be produced by any
    // request. A null price then reaches the service, where comparing it against
    // the sale price throws NPE and surfaces as a 500 instead of a 400.
    // CreateOnlineOrderRequest already annotates its nested list this way; these
    // two were simply missed.
    @Valid
    private List<ProductSpecificationRequest> specifications;

    @Valid
    @NotEmpty(message = "PRODUCT_VARIANTS_REQUIRED")
    private List<ProductVariantRequest> variants;
}
