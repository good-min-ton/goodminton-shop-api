package com.lezh1n.goodminton_shop_api.mappers;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.response.PaymentResponse;
import com.lezh1n.goodminton_shop_api.entities.Payment;

@Component
public class PaymentMapper {

    public PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .vnpayTxnRef(payment.getVnpayTxnRef())
                .vnpayTransactionNo(payment.getVnpayTransactionNo())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
