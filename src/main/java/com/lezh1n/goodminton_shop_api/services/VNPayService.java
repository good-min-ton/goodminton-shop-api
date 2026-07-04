package com.lezh1n.goodminton_shop_api.services;

import java.util.Map;

public interface VNPayService {

    CreatePaymentUrlResult createPaymentUrl(Integer orderId, String ipAddress);

    IpnResult processIpn(Map<String, String> params);

    record CreatePaymentUrlResult(String paymentUrl, String txnRef) {
    }

    record IpnResult(String rspCode, String message) {
    }
}
