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
                .id(order.getId())
                .customerId(order.getCustomer() == null ? null : order.getCustomer().getId())
                .customerName(order.getCustomer() == null ? null : order.getCustomer().getFullName())
                .storeId(order.getStore() == null ? null : order.getStore().getId())
                .storeName(order.getStore() == null ? null : order.getStore().getName())
                .orderType(order.getOrderType())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingCode(order.getShippingCode())
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .recipientAddress(order.getRecipientAddress())
                .recipientEmail(order.getRecipientEmail())
                .note(order.getNote())
                .orderDate(order.getOrderDate())
                .items(order.getOrderItems().stream().map(orderItemMapper::toOrderItemResponse).toList())
                .payments(order.getPayments().stream().map(paymentMapper::toPaymentResponse).toList())
                .build();
    }
}
