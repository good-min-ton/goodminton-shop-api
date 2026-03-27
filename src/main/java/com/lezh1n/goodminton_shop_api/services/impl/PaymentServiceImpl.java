package com.lezh1n.goodminton_shop_api.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.dtos.response.PaymentResponse;
import com.lezh1n.goodminton_shop_api.entities.Order;
import com.lezh1n.goodminton_shop_api.entities.Payment;
import com.lezh1n.goodminton_shop_api.entities.VariantSize;
import com.lezh1n.goodminton_shop_api.enums.OrderStatus;
import com.lezh1n.goodminton_shop_api.enums.OrderType;
import com.lezh1n.goodminton_shop_api.enums.PaymentMethod;
import com.lezh1n.goodminton_shop_api.enums.PaymentStatus;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.mappers.PaymentMapper;
import com.lezh1n.goodminton_shop_api.repositories.OrderRepository;
import com.lezh1n.goodminton_shop_api.repositories.PaymentRepository;
import com.lezh1n.goodminton_shop_api.services.PaymentService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    private final PayOS payOS;

    @Value("${payos.return-url}")
    private String returnUrl;

    @Value("${payos.cancel-url}")
    private String cancelUrl;

    @Override
    @Transactional
    public PaymentResponse createPayment(Integer orderId, PaymentMethod method, BigDecimal amount) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        List<Payment> existingPayment = paymentRepository.findByOrderOrderIdAndStatus(orderId, PaymentStatus.PAID);
        if (!existingPayment.isEmpty()) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_PAID);
        }

        Payment payment = Payment.builder()
                .order(order)
                .method(method)
                .status(PaymentStatus.PENDING)
                .amount(amount)
                .createAt(LocalDateTime.now())
                .build();
        Payment savedPayment = paymentRepository.save(payment);

        if (method == PaymentMethod.BANKING) {
            String paymentLink = createPayOSLink(savedPayment, order);
            savedPayment.setTransactionCode(paymentLink);
            paymentRepository.save(savedPayment);
        }

        return paymentMapper.toPaymentResponse(savedPayment);
    }

    @Override
    public void confirmPayment(Integer paymentId, String transactionCode) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new AppException(ErrorCode.PAYMENT_INVALID_STATUS);
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        payment.setTransactionCode(transactionCode);
        paymentRepository.save(payment);

        Order order = payment.getOrder();

        if (order.getOrderType() == OrderType.ORDER && order.getOrderStatus() == OrderStatus.NEW) {
            order.setOrderStatus(OrderStatus.PAID);
            orderRepository.save(order);
        }
    }

    @Override
    public List<PaymentResponse> getPaymentsByOrderId(Integer orderId) {
        return paymentRepository.findByOrderOrderId(orderId)
                .stream()
                .map(paymentMapper::toPaymentResponse)
                .toList();
    }

    @Override
    public Payment getPaymentObject(Integer paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    @Override
    public void updatePaymentRepository(Payment payment) {
        paymentRepository.save(payment);
    }

    // Private methods
    private String createPayOSLink(Payment payment, Order order) {
        try {
            List<ItemData> items = order.getOrderItems().stream().map(orderItem -> {
                VariantSize variantSize = orderItem.getVariantSize();
                return ItemData.builder()
                        .name(variantSize.getVariant().getProduct().getName() + " - " + variantSize.getSize().getName())
                        .quantity(orderItem.getQuantity())
                        .price(orderItem.getUnitPrice().intValue())
                        .build();
            }).toList();

            PaymentData paymentData = PaymentData.builder()
                    .orderCode(payment.getId().longValue())
                    .amount(payment.getAmount().intValue())
                    .description("Thanh toán đơn hàng #" + order.getId())
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .items(items)
                    .buyerName(order.getName())
                    .buyerEmail(order.getEmail())
                    .buyerPhone(order.getPhone())
                    .build();

            CheckoutResponseData response = payOS.createPaymentLink(paymentData);
            return response.getCheckoutUrl();
        } catch (Exception e) {
            throw new AppException(ErrorCode.PAYMENT_CREATION_FAILED);
        }
    }
}
