package com.lezh1n.goodminton_shop_api.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StoreResponse {
    private Integer id;
    private String name;
    private String address;
    private String contact;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private boolean isCentral;
    private LocalDateTime createdAt;
    private AccountResponse admin;
}
