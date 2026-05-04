package com.lezh1n.goodminton_shop_api.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lezh1n.goodminton_shop_api.common.ApiResponse;
import com.lezh1n.goodminton_shop_api.dtos.request.CreateInStoreOrderRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.CreateOnlineOrderRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.UpdateShippingRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.OrderResponse;
import com.lezh1n.goodminton_shop_api.enums.OrderStatus;
import com.lezh1n.goodminton_shop_api.enums.OrderType;
import com.lezh1n.goodminton_shop_api.services.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ---------- Customer ----------

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<OrderResponse> createOnlineOrder(@Valid @RequestBody CreateOnlineOrderRequest request) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.createOnlineOrder(request))
                .build();
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<Page<OrderResponse>> getMyOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<Page<OrderResponse>>builder()
                .result(orderService.getMyOrders(pageable(page, size, "orderDate", "desc")))
                .build();
    }

    @GetMapping("/my/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<OrderResponse> getMyOrder(@PathVariable Integer orderId) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.getMyOrder(orderId))
                .build();
    }

    @PostMapping("/my/{orderId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<OrderResponse> cancelMyOrder(@PathVariable Integer orderId) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.cancelMyOrder(orderId))
                .build();
    }

    @PostMapping("/my/{orderId}/confirm-received")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<OrderResponse> confirmReceived(@PathVariable Integer orderId) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.confirmReceived(orderId))
                .build();
    }

    // ---------- Store admin ----------

    @PostMapping("/in-store")
    @PreAuthorize("hasRole('STORE_ADMIN')")
    public ApiResponse<OrderResponse> createInStoreOrder(@Valid @RequestBody CreateInStoreOrderRequest request) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.createInStoreOrder(request))
                .build();
    }

    @PostMapping("/{orderId}/preparing")
    @PreAuthorize("hasRole('STORE_ADMIN')")
    public ApiResponse<OrderResponse> markPreparing(@PathVariable Integer orderId) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.markPreparing(orderId))
                .build();
    }

    @PostMapping("/{orderId}/shipping")
    @PreAuthorize("hasRole('STORE_ADMIN')")
    public ApiResponse<OrderResponse> markShipping(@PathVariable Integer orderId,
            @Valid @RequestBody UpdateShippingRequest request) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.markShipping(orderId, request.getShippingCode()))
                .build();
    }

    @PostMapping("/{orderId}/delivered")
    @PreAuthorize("hasRole('STORE_ADMIN')")
    public ApiResponse<OrderResponse> markDelivered(@PathVariable Integer orderId) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.markDelivered(orderId))
                .build();
    }

    @GetMapping("/store")
    @PreAuthorize("hasRole('STORE_ADMIN')")
    public ApiResponse<Page<OrderResponse>> getStoreOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.<Page<OrderResponse>>builder()
                .result(orderService.getStoreOrders(pageable(page, size, "orderDate", "desc")))
                .build();
    }

    // ---------- Super admin ----------

    @PostMapping("/{orderId}/confirm")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<OrderResponse> confirmOrder(@PathVariable Integer orderId) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.confirmOrder(orderId))
                .build();
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable Integer orderId) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.getOrderById(orderId))
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Page<OrderResponse>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) OrderType type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.<Page<OrderResponse>>builder()
                .result(orderService.getAllOrders(status, type, pageable(page, size, "orderDate", "desc")))
                .build();
    }

    private Pageable pageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        return PageRequest.of(Math.max(0, page - 1), size, sort);
    }
}
