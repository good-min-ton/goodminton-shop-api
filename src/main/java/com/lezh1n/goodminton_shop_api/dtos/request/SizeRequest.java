package com.lezh1n.goodminton_shop_api.dtos.request;

import com.lezh1n.goodminton_shop_api.enums.SizeType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SizeRequest {
    
    @NotBlank(message = "SIZE_NAME_BLANK")
    private String name;
    
    @NotNull(message = "SIZE_TYPE_BLANK")
    private SizeType type;
}
