package com.lezh1n.goodminton_shop_api.services;

public interface PayOSService {

    CreatePaymentUrlResult createPaymentUrl(Integer orderId);

    WebhookResult processWebhook(Object rawBody);

    record CreatePaymentUrlResult(String paymentUrl, long orderCode) {
    }

    record WebhookResult(boolean success, String message) {
    }
}
