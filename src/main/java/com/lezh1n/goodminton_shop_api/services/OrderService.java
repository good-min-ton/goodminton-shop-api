package com.lezh1n.goodminton_shop_api.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.lezh1n.goodminton_shop_api.dtos.request.CreateInStoreOrderRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.CreateOnlineOrderRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.OrderResponse;
import com.lezh1n.goodminton_shop_api.enums.OrderStatus;
import com.lezh1n.goodminton_shop_api.enums.OrderType;

public interface OrderService {

    // Customer
    OrderResponse createOnlineOrder(CreateOnlineOrderRequest request);

    OrderResponse getMyOrder(Integer orderId);

    Page<OrderResponse> getMyOrders(Pageable pageable);

    OrderResponse cancelMyOrder(Integer orderId);

    OrderResponse confirmReceived(Integer orderId);

    // Store admin
    OrderResponse createInStoreOrder(CreateInStoreOrderRequest request);

    OrderResponse markPreparing(Integer orderId);

    OrderResponse markShipping(Integer orderId, String shippingCode);

    OrderResponse markDelivered(Integer orderId);

    Page<OrderResponse> getStoreOrders(Pageable pageable);

    // Super admin
    OrderResponse confirmOrder(Integer orderId);

    OrderResponse getOrderById(Integer orderId);

    Page<OrderResponse> getAllOrders(OrderStatus status, OrderType type, Pageable pageable);

    // Scheduled jobs
    int autoCompleteDeliveredOrders();

    int cancelExpiredProviderPaymentOrders();
}
