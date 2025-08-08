package com.lezh1n.goodminton_shop_api.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.dtos.response.PaymentResponse;
import com.lezh1n.goodminton_shop_api.entities.Order;
import com.lezh1n.goodminton_shop_api.entities.Payment;
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

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;

    @Override
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

}
