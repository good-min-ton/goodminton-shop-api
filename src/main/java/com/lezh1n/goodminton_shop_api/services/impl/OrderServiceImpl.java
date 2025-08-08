package com.lezh1n.goodminton_shop_api.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.dtos.request.InventoryAllocationRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.OrderAllocationRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.OrderItemAllocationRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.OrderItemRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.OrderRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.OrderResponse;
import com.lezh1n.goodminton_shop_api.entities.Account;
import com.lezh1n.goodminton_shop_api.entities.Inventory;
import com.lezh1n.goodminton_shop_api.entities.Order;
import com.lezh1n.goodminton_shop_api.entities.OrderItem;
import com.lezh1n.goodminton_shop_api.entities.OrderItemInventoryAllocation;
import com.lezh1n.goodminton_shop_api.entities.ProductDiscount;
import com.lezh1n.goodminton_shop_api.entities.VariantSize;
import com.lezh1n.goodminton_shop_api.enums.OrderStatus;
import com.lezh1n.goodminton_shop_api.enums.OrderType;
import com.lezh1n.goodminton_shop_api.enums.UserRole;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.mappers.OrderMapper;
import com.lezh1n.goodminton_shop_api.repositories.InventoryAllocationRepository;
import com.lezh1n.goodminton_shop_api.repositories.InventoryRepository;
import com.lezh1n.goodminton_shop_api.repositories.OrderItemRepository;
import com.lezh1n.goodminton_shop_api.repositories.OrderRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductDiscountRepository;
import com.lezh1n.goodminton_shop_api.repositories.StoreRepository;
import com.lezh1n.goodminton_shop_api.repositories.VariantSizeRepository;
import com.lezh1n.goodminton_shop_api.services.CartService;
import com.lezh1n.goodminton_shop_api.services.OrderService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final StoreRepository storeRepository;
    private final InventoryRepository inventoryRepository;
    private final VariantSizeRepository variantSizeRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryAllocationRepository inventoryAllocationRepository;
    private final ProductDiscountRepository productDiscountRepository;
    private final CartService cartService;
    private final OrderMapper orderMapper;

    @Override
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderResponse createOrder(OrderRequest request) {
        Account customer = getCurrentAuthentication();
        if (customer.getRole() != UserRole.CUSTOMER) {
            throw new AppException(ErrorCode.AUTH_UNAUTHORIZED);
        }

        for (OrderItemRequest item : request.getItems()) {
            Integer totalQuantity = inventoryRepository.sumQuantityByVariantSize(item.getVariantSizeId());
            if (totalQuantity == null || totalQuantity < item.getQuantity()) {
                throw new AppException(ErrorCode.ORDER_INVENTORY_INSUFFICIENT);
            }
        }

        Order order = Order.builder()
                .customer(customer)
                .orderDate(LocalDateTime.now())
                .totalAmount(BigDecimal.ZERO)
                .name(request.getName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .email(request.getEmail())
                .note(request.getNote())
                .orderStatus(OrderStatus.NEW)
                .orderType(OrderType.ORDER)
                .orderItems(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemRequest item : request.getItems()) {
            VariantSize variantSize = variantSizeRepository.findById(item.getVariantSizeId())
                    .orElseThrow(() -> new AppException(ErrorCode.VARIANT_SIZE_NOT_FOUND));

            BigDecimal unitPrice = variantSize.getPrice();
            Optional<ProductDiscount> discount = productDiscountRepository
                    .findActiveDiscountByVariantSizeId(item.getVariantSizeId(), LocalDateTime.now());
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .variantSize(variantSize)
                    .quantity(item.getQuantity())
                    .unitPrice(variantSize.getPrice())
                    .build();
            order.getOrderItems().add(orderItem);
            totalAmount = totalAmount.add(variantSize.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        cartService.clearCart(customer.getAccountId());

        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @PreAuthorize("hasRole('STORE_ADMIN')")
    public OrderResponse createStoreOrder(OrderRequest request, Integer storeId) {
        Account admin = getCurrentAuthentication();
        if (admin.getRole() != UserRole.STORE_ADMIN) {
            throw new AppException(ErrorCode.AUTH_UNAUTHORIZED);
        }

        if (!storeRepository.existsById(storeId)) {
            throw new AppException(ErrorCode.STORE_NOT_FOUND);
        }

        for (OrderItemRequest item : request.getItems()) {
            Inventory inventory = inventoryRepository.findByVariantAndStore(item.getVariantSizeId(), storeId)
                    .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));
            if (inventory.getQuantity() < item.getQuantity()) {
                throw new AppException(ErrorCode.ORDER_INVENTORY_INSUFFICIENT);
            }
        }

        Order order = Order.builder()
                .customer(null)
                .orderDate(LocalDateTime.now())
                .totalAmount(BigDecimal.ZERO)
                .name(request.getName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .email(request.getEmail())
                .note(request.getNote())
                .orderStatus(OrderStatus.PAID)
                .orderType(OrderType.DIRECT)
                .orderItems(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemRequest item : request.getItems()) {
            VariantSize variantSize = variantSizeRepository.findById(item.getVariantSizeId())
                    .orElseThrow(() -> new AppException(ErrorCode.VARIANT_SIZE_NOT_FOUND));
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .variantSize(variantSize)
                    .quantity(item.getQuantity())
                    .unitPrice(variantSize.getPrice())
                    .build();
            order.getOrderItems().add(orderItem);
            totalAmount = totalAmount.add(variantSize.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

            Inventory inventory = inventoryRepository.findByVariantAndStore(item.getVariantSizeId(), storeId)
                    .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

            inventory.setQuantity(inventory.getQuantity() - item.getQuantity());
            inventory.setUpdatedAt(LocalDateTime.now());
            inventoryRepository.save(inventory);

            OrderItemInventoryAllocation allocation = OrderItemInventoryAllocation.builder()
                    .orderItem(orderItem)
                    .inventory(inventory)
                    .quantity(item.getQuantity())
                    .build();
            inventoryAllocationRepository.save(allocation);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        orderItemRepository.flush();

        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    public OrderResponse allocateOrder(OrderAllocationRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getOrderStatus() != OrderStatus.NEW) {
            throw new AppException(ErrorCode.ORDER_INVALID_STATUS);
        }

        for (OrderItemAllocationRequest itemAllocation : request.getAllocations()) {
            OrderItem orderItem = orderItemRepository.findById(itemAllocation.getOrderItemId())
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_ITEM_NOT_FOUND));

            int totalAllocated = itemAllocation.getInventoryAllocations().stream()
                    .mapToInt(InventoryAllocationRequest::getQuantity).sum();
            if (totalAllocated != orderItem.getQuantity()) {
                throw new AppException(ErrorCode.ALLOCATION_QUANTITY_INVALID);
            }

            for (InventoryAllocationRequest alloc : itemAllocation.getInventoryAllocations()) {
                Inventory inventory = inventoryRepository.findById(alloc.getInventoryId())
                        .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

                if (inventory.getQuantity() < alloc.getQuantity()) {
                    throw new AppException(ErrorCode.ORDER_INVENTORY_INSUFFICIENT);
                }
                if (!inventory.getVariantSize().getVariantSizeId()
                        .equals(orderItem.getVariantSize().getVariantSizeId())) {
                    throw new AppException(ErrorCode.ALLOCATION_VARIANT_SIZE_INVALID);
                }

                inventory.setQuantity(inventory.getQuantity() - alloc.getQuantity());
                inventory.setUpdatedAt(LocalDateTime.now());
                inventoryRepository.save(inventory);

                OrderItemInventoryAllocation allocation = OrderItemInventoryAllocation.builder()
                        .orderItem(orderItem)
                        .inventory(inventory)
                        .quantity(alloc.getQuantity())
                        .build();
                inventoryAllocationRepository.save(allocation);
            }
        }
        order.setOrderStatus(OrderStatus.PAID);
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(updatedOrder);
    }

    @Override
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderResponse cancelOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getOrderStatus() == OrderStatus.COMPLETED) {
            throw new AppException(ErrorCode.ORDER_CANNOT_CANCEL_COMPLETED);
        }

        if (order.getOrderStatus() == OrderStatus.CANCEL) {
            throw new AppException(ErrorCode.ORDER_CANCEL_ALREADY_CANCELLED);
        }

        if (order.getOrderStatus() == OrderStatus.PAID || order.getOrderStatus() == OrderStatus.SHIPPED) {
            for (OrderItem item : order.getOrderItems()) {
                List<OrderItemInventoryAllocation> allocations = inventoryAllocationRepository
                        .findByOrderItemOrderItemId(item.getOrderItemId());
                for (OrderItemInventoryAllocation allocation : allocations) {
                    Inventory inventory = allocation.getInventory();
                    inventory.setQuantity(inventory.getQuantity() + allocation.getQuantity());
                    inventory.setUpdatedAt(LocalDateTime.now());
                    inventoryRepository.save(inventory);
                }
                inventoryAllocationRepository.deleteAll(allocations);
            }
        }

        order.setOrderStatus(OrderStatus.CANCEL);
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(updatedOrder);
    }

    @Override
    public OrderResponse getOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return orderMapper.toOrderResponse(order);
    }

    @Override
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByOrderStatus(status).stream().map(orderMapper::toOrderResponse).toList();
    }

    private Account getCurrentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.AUTH_UNAUTHENTICATED);
        }

        return (Account) authentication.getPrincipal();
    }
}
