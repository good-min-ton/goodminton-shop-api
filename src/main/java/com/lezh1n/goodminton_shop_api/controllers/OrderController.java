package com.lezh1n.goodminton_shop_api.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lezh1n.goodminton_shop_api.common.ApiResponse;
import com.lezh1n.goodminton_shop_api.dtos.request.CartItemRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.OrderAllocationRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.OrderRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.CartResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.OrderResponse;
import com.lezh1n.goodminton_shop_api.enums.OrderStatus;
import com.lezh1n.goodminton_shop_api.services.CartService;
import com.lezh1n.goodminton_shop_api.services.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    @PostMapping
    public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.createOrder(request))
                .build();
    }

    @PostMapping("/store")
    public ApiResponse<OrderResponse> createStoreOrder(
            @Valid @RequestBody OrderRequest request,
            @RequestParam Integer storeId) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.createStoreOrder(request, storeId))
                .build();
    }

    @PostMapping("/allocate/{orderId}")
    public ApiResponse<OrderResponse> allocateOrder(@PathVariable Integer orderId,
            @Valid @RequestBody OrderAllocationRequest request) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.allocateOrder(orderId, request))
                .build();
    }

    @PutMapping("/{orderId}/cancel")
    public ApiResponse<OrderResponse> cancelOrder(@PathVariable Integer orderId) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.cancelOrder(orderId))
                .build();
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrder(@PathVariable Integer orderId) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.getOrder(orderId))
                .build();
    }

    @GetMapping("/status/{status}")
    public ApiResponse<List<OrderResponse>> getOrdersByStatus(@PathVariable OrderStatus status) {
        return ApiResponse.<List<OrderResponse>>builder()
                .result(orderService.getOrdersByStatus(status))
                .build();
    }

    @PostMapping("/cart")
    public ApiResponse<String> addToCart(
            @RequestParam Integer accountId,
            @Valid @RequestBody CartItemRequest request) {
        cartService.addToCart(accountId, request);
        return ApiResponse.<String>builder()
                .result("Thêm sản phẩm vào giỏ hàng thành công")
                .build();
    }

    @GetMapping("/cart")
    public ApiResponse<CartResponse> getCart(@RequestParam Integer accountId) {
        CartResponse cart = cartService.getCart(accountId);
        return ApiResponse.<CartResponse>builder()
                .result(cart != null ? cart : CartResponse.builder().items(new ArrayList<>()).build())
                .build();
    }
}
