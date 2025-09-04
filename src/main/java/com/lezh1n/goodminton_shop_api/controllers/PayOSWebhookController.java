package com.lezh1n.goodminton_shop_api.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lezh1n.goodminton_shop_api.dtos.ApiResponse;
import com.lezh1n.goodminton_shop_api.entities.Payment;
import com.lezh1n.goodminton_shop_api.enums.PaymentStatus;
import com.lezh1n.goodminton_shop_api.services.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.payos.PayOS;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

@RestController
@RequestMapping("/api/payos")
@RequiredArgsConstructor
@Slf4j
public class PayOSWebhookController {
    private final PaymentService paymentService;
    private final PayOS payOS;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @PostMapping("/webhook")
    public ApiResponse<String> handleWebhook(@RequestBody String body) {
        try {
            Webhook webhook = objectMapper.readValue(body, Webhook.class);
            WebhookData webhookData = payOS.verifyPaymentWebhookData(webhook);
            Long orderCode = webhookData.getOrderCode();
            String status = webhookData.getCode();
            String reference = webhookData.getReference();

            if (orderCode == null) {
                log.warn("Webhook missing orderCode, ignore");
                return ApiResponse.<String>builder()
                        .result("OK")
                        .build();
            }

            Payment payment = null;
            try {
                payment = paymentService.getPaymentObject(orderCode.intValue());
            } catch (Exception e) {
                log.warn("Payment not found for orderCode={}, likely test payload. Ignore.", orderCode);
                return ApiResponse.<String>builder()
                        .result("OK")
                        .build();
            }

            if (payment.getStatus() == PaymentStatus.PAID) {
                log.info("Payment {} already PAID, skip.", payment.getPaymentId());
                return ApiResponse.<String>builder()
                        .result("OK")
                        .build();
            }

            if (isSuccessStatus(status)) {
                paymentService.confirmPayment(payment.getPaymentId(), reference);
            } else if (isFailStatus(status)) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentService.updatePaymentRepository(payment);
            } else {
                log.info("Unknown/ignore status={}, keep payment pending", status);
            }

            return ApiResponse.<String>builder()
                    .result("OK")
                    .build();
        } catch (Exception e) {
            log.error("Webhook processing failed (but returning 200): {}", e.getMessage(), e);
            return ApiResponse.<String>builder()
                    .result("OK")
                    .build();
        }
    }

    private boolean isSuccessStatus(String code) {
        // PayOS có thể trả "00" hoặc "PAYMENT_SUCCESS" (tuỳ SDK/version)
        return "00".equals(code) || "PAYMENT_SUCCESS".equalsIgnoreCase(code) || "PAID".equalsIgnoreCase(code);
    }

    private boolean isFailStatus(String code) {
        return "FAILED".equalsIgnoreCase(code) || "CANCELLED".equalsIgnoreCase(code) || "01".equals(code);
    }
}
