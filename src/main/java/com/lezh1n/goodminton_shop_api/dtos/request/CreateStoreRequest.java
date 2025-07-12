package com.lezh1n.goodminton_shop_api.dtos.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CreateStoreRequest {

    @NotBlank(message = "STORE_NAME_BLANK")
    private String name;

    @NotBlank(message = "STORE_ADDRESS_BLANK")
    private String address;

    @NotBlank(message = "STORE_ADDRESS_BLANK")
    @Size(min = 10, max = 12, message = "AUTH_INVALID_PHONE")
    private String contact;

    @NotNull(message = "STORE_ADDRESS_BLANK")
    private BigDecimal longitude;

    @NotNull(message = "STORE_ADDRESS_BLANK")
    private BigDecimal latitude;

    @NotNull(message = "STORE_ADMIN_BLANK")
    private Integer adminId;
}
