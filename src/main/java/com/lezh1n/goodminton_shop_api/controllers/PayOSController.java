package com.lezh1n.goodminton_shop_api.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lezh1n.goodminton_shop_api.common.ApiResponse;
import com.lezh1n.goodminton_shop_api.dtos.request.CreatePaymentUrlRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.CreatePayOSPaymentResponse;
import com.lezh1n.goodminton_shop_api.services.PayOSService;
import com.lezh1n.goodminton_shop_api.services.PayOSService.CreatePaymentUrlResult;
import com.lezh1n.goodminton_shop_api.services.PayOSService.WebhookResult;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payos")
@RequiredArgsConstructor
public class PayOSController {

    private final PayOSService payOSService;

    @PostMapping("/create-payment-url")
    public ApiResponse<CreatePayOSPaymentResponse> createPaymentUrl(
            @Valid @RequestBody CreatePaymentUrlRequest request) {
        CreatePaymentUrlResult result = payOSService.createPaymentUrl(request.getOrderId());
        return ApiResponse.<CreatePayOSPaymentResponse>builder()
                .result(CreatePayOSPaymentResponse.builder()
                        .paymentUrl(result.paymentUrl())
                        .orderCode(result.orderCode())
                        .build())
                .build();
    }

    @PostMapping("/webhook")
    public ApiResponse<String> webhook(@RequestBody Object body) {
        WebhookResult result = payOSService.processWebhook(body);
        return ApiResponse.<String>builder()
                .result(result.message())
                .build();
    }
}
