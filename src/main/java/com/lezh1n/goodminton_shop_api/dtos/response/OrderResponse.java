package com.lezh1n.goodminton_shop_api.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lezh1n.goodminton_shop_api.enums.OrderStatus;
import com.lezh1n.goodminton_shop_api.enums.OrderType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {
    private Integer id;
    private Integer customerId;
    private String customerName;
    private Integer storeId;
    private String storeName;
    private OrderType orderType;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingCode;
    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;
    private String recipientEmail;
    private String note;
    private LocalDateTime orderDate;
    private List<OrderItemResponse> items;
    private List<PaymentResponse> payments;
}
