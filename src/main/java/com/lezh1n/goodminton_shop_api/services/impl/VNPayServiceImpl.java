package com.lezh1n.goodminton_shop_api.services.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.configurations.VNPayProperties;
import com.lezh1n.goodminton_shop_api.entities.Order;
import com.lezh1n.goodminton_shop_api.entities.Payment;
import com.lezh1n.goodminton_shop_api.enums.OrderStatus;
import com.lezh1n.goodminton_shop_api.enums.PaymentMethod;
import com.lezh1n.goodminton_shop_api.enums.PaymentStatus;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.repositories.OrderRepository;
import com.lezh1n.goodminton_shop_api.repositories.PaymentRepository;
import com.lezh1n.goodminton_shop_api.services.VNPayService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayServiceImpl implements VNPayService {

    private static final DateTimeFormatter VNP_DATE_FORMAT = DateTimeFormatter
            .ofPattern("yyyyMMddHHmmss")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    private static final String VERSION = "2.1.0";
    private static final String COMMAND = "pay";
    private static final String CURRENCY = "VND";
    private static final String LOCALE = "vn";
    private static final String ORDER_TYPE = "other";
    private static final String SUCCESS_CODE = "00";

    private final VNPayProperties props;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public CreatePaymentUrlResult createPaymentUrl(Integer orderId, String ipAddress) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(ErrorCode.ORDER_INVALID_STATUS);
        }

        if (!paymentRepository.findByOrder_IdAndStatus(orderId, PaymentStatus.PAID).isEmpty()) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_PAID);
        }

        String txnRef = orderId + "-" + System.currentTimeMillis();
        Instant now = Instant.now();

        paymentRepository.save(Payment.builder()
                .order(order)
                .method(PaymentMethod.VNPAY)
                .status(PaymentStatus.PENDING)
                .amount(order.getTotalAmount())
                .vnpayTxnRef(txnRef)
                .createdAt(LocalDateTime.now())
                .build());

        long amountUnits = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValueExact();

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", VERSION);
        params.put("vnp_Command", COMMAND);
        params.put("vnp_TmnCode", props.getTmnCode());
        params.put("vnp_Amount", String.valueOf(amountUnits));
        params.put("vnp_CurrCode", CURRENCY);
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", "Thanh toan don hang " + orderId);
        params.put("vnp_OrderType", ORDER_TYPE);
        params.put("vnp_Locale", LOCALE);
        params.put("vnp_ReturnUrl", props.getReturnUrl());
        params.put("vnp_IpAddr", ipAddress);
        params.put("vnp_CreateDate", VNP_DATE_FORMAT.format(now));
        params.put("vnp_ExpireDate", VNP_DATE_FORMAT.format(
                now.plus(props.getPaymentTimeoutMinutes(), ChronoUnit.MINUTES)));

        String hashData = VNPaySignatureUtil.buildHashData(params);
        String secureHash = VNPaySignatureUtil.hmacSHA512(props.getHashSecret(), hashData);
        String queryString = VNPaySignatureUtil.buildQueryString(params);
        String paymentUrl = props.getPayUrl() + "?" + queryString + "&vnp_SecureHash=" + secureHash;

        return new CreatePaymentUrlResult(paymentUrl, txnRef);
    }

    @Override
    @Transactional
    public IpnResult processIpn(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        if (!VNPaySignatureUtil.verify(props.getHashSecret(), params, receivedHash)) {
            log.warn("VNPay IPN invalid signature, txnRef={}", params.get("vnp_TxnRef"));
            return new IpnResult("97", "Invalid signature");
        }

        String txnRef = params.get("vnp_TxnRef");
        Payment payment = paymentRepository.findByVnpayTxnRef(txnRef).orElse(null);
        if (payment == null) {
            return new IpnResult("01", "Order not found");
        }

        long expectedAmount = payment.getAmount().multiply(BigDecimal.valueOf(100)).longValueExact();
        long receivedAmount;
        try {
            receivedAmount = Long.parseLong(params.getOrDefault("vnp_Amount", "0"));
        } catch (NumberFormatException e) {
            return new IpnResult("04", "Invalid amount");
        }
        if (expectedAmount != receivedAmount) {
            return new IpnResult("04", "Invalid amount");
        }

        // VNPay may retry IPN — return 02 if already processed to keep handler idempotent.
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return new IpnResult("02", "Order already confirmed");
        }

        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");

        payment.setVnpayTransactionNo(params.get("vnp_TransactionNo"));
        payment.setVnpayBankCode(params.get("vnp_BankCode"));
        payment.setVnpayResponseCode(responseCode);

        if (SUCCESS_CODE.equals(responseCode) && SUCCESS_CODE.equals(transactionStatus)) {
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }
        paymentRepository.save(payment);

        return new IpnResult(SUCCESS_CODE, "Confirm Success");
    }

}
