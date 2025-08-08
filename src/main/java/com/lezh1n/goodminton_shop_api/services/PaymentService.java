package com.lezh1n.goodminton_shop_api.services;

import java.math.BigDecimal;
import java.util.List;

import com.lezh1n.goodminton_shop_api.dtos.response.PaymentResponse;
import com.lezh1n.goodminton_shop_api.enums.PaymentMethod;

public interface PaymentService {
    PaymentResponse createPayment(Integer orderId, PaymentMethod method, BigDecimal amount);

    void confirmPayment(Integer paymentId, String transactionCode);

    List<PaymentResponse> getPaymentsByOrderId(Integer orderId);
}
