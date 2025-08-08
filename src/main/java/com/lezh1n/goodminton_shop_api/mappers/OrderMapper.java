package com.lezh1n.goodminton_shop_api.mappers;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.response.OrderResponse;
import com.lezh1n.goodminton_shop_api.entities.Order;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final OrderItemMapper orderItemMapper;
    private final PaymentMapper paymentMapper;

    public OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomer().getAccountId())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .name(order.getName())
                .phone(order.getPhone())
                .address(order.getAddress())
                .email(order.getEmail())
                .note(order.getNote())
                .status(order.getOrderStatus())
                .items(order.getOrderItems().stream().map(orderItemMapper::toOrderItemResponse).toList())
                .payments(order.getPayments().stream().map(paymentMapper::toPaymentResponse).toList())
                .build();
    }
}
