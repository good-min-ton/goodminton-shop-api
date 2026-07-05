package com.lezh1n.goodminton_shop_api.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.configurations.PayOSProperties;
import com.lezh1n.goodminton_shop_api.entities.Order;
import com.lezh1n.goodminton_shop_api.entities.Payment;
import com.lezh1n.goodminton_shop_api.enums.OrderStatus;
import com.lezh1n.goodminton_shop_api.enums.PaymentMethod;
import com.lezh1n.goodminton_shop_api.enums.PaymentStatus;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.repositories.OrderRepository;
import com.lezh1n.goodminton_shop_api.repositories.PaymentRepository;
import com.lezh1n.goodminton_shop_api.services.PayOSService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.WebhookData;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayOSServiceImpl implements PayOSService {

    private final PayOS payOS;
    private final PayOSProperties props;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public CreatePaymentUrlResult createPaymentUrl(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(ErrorCode.ORDER_INVALID_STATUS);
        }

        if (!paymentRepository.findByOrder_IdAndStatus(orderId, PaymentStatus.PAID).isEmpty()) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_PAID);
        }

        long orderCode = System.currentTimeMillis() / 1000;
        long amount = order.getTotalAmount().longValueExact();
        String description = "DH" + orderId;

        PaymentLinkItem item = PaymentLinkItem.builder()
                .name("Goodminton order #" + orderId)
                .quantity(1)
                .price(amount)
                .build();

        CreatePaymentLinkRequest request = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .description(description)
                .amount(amount)
                .item(item)
                .returnUrl(props.getReturnUrl())
                .cancelUrl(props.getCancelUrl())
                .build();

        CreatePaymentLinkResponse response;
        try {
            response = payOS.paymentRequests().create(request);
        } catch (Exception e) {
            log.error("PayOS create payment link failed for order {}", orderId, e);
            throw new AppException(ErrorCode.SYSTEM_INTERNAL_ERROR);
        }

        paymentRepository.save(Payment.builder()
                .order(order)
                .method(PaymentMethod.PAYOS)
                .status(PaymentStatus.PENDING)
                .amount(order.getTotalAmount())
                .payosOrderCode(orderCode)
                .payosPaymentLinkId(response.getPaymentLinkId())
                .createdAt(LocalDateTime.now())
                .build());

        return new CreatePaymentUrlResult(response.getCheckoutUrl(), orderCode);
    }

    @Override
    @Transactional
    public WebhookResult processWebhook(Object rawBody) {
        WebhookData data;
        try {
            data = payOS.webhooks().verify(rawBody);
        } catch (Exception e) {
            log.warn("PayOS webhook signature verification failed", e);
            return new WebhookResult(false, "Invalid signature");
        }

        Long orderCode = data.getOrderCode();
        if (orderCode == null) {
            return new WebhookResult(false, "Missing orderCode");
        }

        Optional<Payment> paymentOpt = paymentRepository.findByPayosOrderCode(orderCode);
        if (paymentOpt.isEmpty()) {
            log.warn("PayOS webhook received unknown orderCode={}", orderCode);
            return new WebhookResult(false, "Order not found");
        }
        Payment payment = paymentOpt.get();

        long expectedAmount = payment.getAmount().longValueExact();
        long receivedAmount = data.getAmount() == null ? 0L : data.getAmount().longValue();
        if (expectedAmount != receivedAmount) {
            log.warn("PayOS webhook amount mismatch orderCode={} expected={} got={}",
                    orderCode, expectedAmount, receivedAmount);
            return new WebhookResult(false, "Invalid amount");
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            return new WebhookResult(true, "Order already processed");
        }

        payment.setPayosReference(data.getReference());
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
        }

        log.info("PayOS payment confirmed for order {} (amount {})", order.getId(),
                BigDecimal.valueOf(receivedAmount));
        return new WebhookResult(true, "Confirmed");
    }
}
