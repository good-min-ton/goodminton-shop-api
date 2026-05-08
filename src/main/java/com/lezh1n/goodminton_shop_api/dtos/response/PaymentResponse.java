package com.lezh1n.goodminton_shop_api.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lezh1n.goodminton_shop_api.enums.PaymentMethod;
import com.lezh1n.goodminton_shop_api.enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {
    private Integer id;
    private PaymentMethod method;
    private PaymentStatus status;
    private BigDecimal amount;
    private String vnpayTxnRef;
    private String vnpayTransactionNo;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
