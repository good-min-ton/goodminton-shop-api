package com.lezh1n.goodminton_shop_api.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lezh1n.goodminton_shop_api.enums.OrderStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {
    private Integer orderId;
    private Integer customerId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String name;
    private String phone;
    private String address;
    private String email;
    private String note;
    private OrderStatus status;
    private List<OrderItemResponse> items;
}
