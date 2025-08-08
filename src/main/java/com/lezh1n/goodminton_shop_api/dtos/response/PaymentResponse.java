package com.lezh1n.goodminton_shop_api.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lezh1n.goodminton_shop_api.enums.PaymentMethod;
import com.lezh1n.goodminton_shop_api.enums.PaymentStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {
    private Integer paymentId;
    private PaymentMethod method;
    private PaymentStatus status;
    private BigDecimal amount;
    private LocalDateTime paidAt;
    private String transactionCode;
    private LocalDateTime createAt;
}
