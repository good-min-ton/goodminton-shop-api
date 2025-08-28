package com.lezh1n.goodminton_shop_api.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lezh1n.goodminton_shop_api.dtos.ApiResponse;
import com.lezh1n.goodminton_shop_api.entities.Payment;
import com.lezh1n.goodminton_shop_api.enums.PaymentStatus;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.services.PaymentService;

import lombok.RequiredArgsConstructor;
import vn.payos.PayOS;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

@RestController
@RequestMapping("api/payos")
@RequiredArgsConstructor
public class PayOSWebhookController {
    private final PaymentService paymentService;
    private final PayOS payOS;

    @PostMapping("/webhook")
    public ApiResponse<String> handleWebhook(@RequestBody Webhook webhook) {
        try {
            WebhookData webhookData = payOS.verifyPaymentWebhookData(webhook);
            String orderCode = String.valueOf(webhookData.getOrderCode());
            String status = webhookData.getCode();
            String transactionId = webhookData.getReference();

            Payment payment = paymentService.getPaymentObject(Integer.valueOf(orderCode));

            if ("PAID".equals(status)) {
                paymentService.confirmPayment(payment.getPaymentId(), transactionId);
            } else if ("CANCELLED".equals(status) || "FAILED".equals(status)) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentService.updatePaymentRepository(payment);
            }

            return ApiResponse.<String>builder()
                    .result("Verify webhook payment successfully")
                    .build();
        } catch (Exception e) {
            throw new AppException(ErrorCode.PAYMENT_WEBHOOK_FAILED);
        }
    }
}
