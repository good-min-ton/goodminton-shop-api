package com.lezh1n.goodminton_shop_api.controllers;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import com.lezh1n.goodminton_shop_api.common.ApiResponse;
import com.lezh1n.goodminton_shop_api.configurations.VNPayProperties;
import com.lezh1n.goodminton_shop_api.dtos.request.CreatePaymentUrlRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.CreatePaymentUrlResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.VNPayIpnResponse;
import com.lezh1n.goodminton_shop_api.services.VNPayService;
import com.lezh1n.goodminton_shop_api.services.VNPayService.CallbackResult;
import com.lezh1n.goodminton_shop_api.services.VNPayService.CreatePaymentUrlResult;
import com.lezh1n.goodminton_shop_api.services.VNPayService.IpnResult;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vnpay")
@RequiredArgsConstructor
public class VNPayController {

    private final VNPayService vnPayService;
    private final VNPayProperties props;

    @PostMapping("/create-payment-url")
    public ApiResponse<CreatePaymentUrlResponse> createPaymentUrl(
            @Valid @RequestBody CreatePaymentUrlRequest request,
            HttpServletRequest httpRequest) {
        CreatePaymentUrlResult result = vnPayService.createPaymentUrl(
                request.getOrderId(), getClientIp(httpRequest));
        return ApiResponse.<CreatePaymentUrlResponse>builder()
                .result(CreatePaymentUrlResponse.builder()
                        .paymentUrl(result.paymentUrl())
                        .txnRef(result.txnRef())
                        .build())
                .build();
    }

    @GetMapping("/callback")
    public RedirectView callback(@RequestParam Map<String, String> params) {
        CallbackResult result = vnPayService.verifyCallback(params);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(props.getFrontendReturnUrl())
                .queryParam("success", result.success())
                .queryParam("validSignature", result.validSignature());
        if (result.orderId() != null) {
            builder.queryParam("orderId", result.orderId());
        }
        if (result.responseCode() != null) {
            builder.queryParam("responseCode", result.responseCode());
        }
        return new RedirectView(builder.build().toUriString());
    }

    @GetMapping("/ipn")
    public VNPayIpnResponse ipn(@RequestParam Map<String, String> params) {
        IpnResult result = vnPayService.processIpn(params);
        return new VNPayIpnResponse(result.rspCode(), result.message());
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
