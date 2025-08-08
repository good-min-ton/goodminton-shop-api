package com.lezh1n.goodminton_shop_api.services;

import java.util.List;

import com.lezh1n.goodminton_shop_api.dtos.request.OrderAllocationRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.OrderRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.OrderResponse;
import com.lezh1n.goodminton_shop_api.enums.OrderStatus;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);
    OrderResponse createStoreOrder(OrderRequest request, Integer storeId); 
    OrderResponse allocateOrder(OrderAllocationRequest request);
    OrderResponse cancelOrder(Integer orderId);
    OrderResponse getOrder(Integer orderId);
    List<OrderResponse> getOrdersByStatus(OrderStatus status);
}
