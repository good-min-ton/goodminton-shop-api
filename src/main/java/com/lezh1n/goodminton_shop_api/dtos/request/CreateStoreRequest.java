package com.lezh1n.goodminton_shop_api.dtos.request;

import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class CreateStoreRequest {
    private String name;
    private String address;
    private String contact;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer adminId;
}
