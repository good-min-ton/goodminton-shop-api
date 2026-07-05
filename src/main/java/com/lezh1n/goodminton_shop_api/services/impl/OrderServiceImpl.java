package com.lezh1n.goodminton_shop_api.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.configurations.CacheConfig;
import com.lezh1n.goodminton_shop_api.configurations.OrderProperties;
import com.lezh1n.goodminton_shop_api.configurations.PayOSProperties;
import com.lezh1n.goodminton_shop_api.configurations.VNPayProperties;
import com.lezh1n.goodminton_shop_api.dtos.request.CreateInStoreOrderRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.CreateOnlineOrderRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.OrderItemRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.OrderResponse;
import com.lezh1n.goodminton_shop_api.entities.Account;
import com.lezh1n.goodminton_shop_api.entities.Order;
import com.lezh1n.goodminton_shop_api.entities.OrderItem;
import com.lezh1n.goodminton_shop_api.entities.Payment;
import com.lezh1n.goodminton_shop_api.entities.ProductVariant;
import com.lezh1n.goodminton_shop_api.entities.Store;
import com.lezh1n.goodminton_shop_api.enums.OrderStatus;
import com.lezh1n.goodminton_shop_api.enums.OrderType;
import com.lezh1n.goodminton_shop_api.enums.PaymentMethod;
import com.lezh1n.goodminton_shop_api.enums.PaymentStatus;
import com.lezh1n.goodminton_shop_api.enums.UserRole;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.mappers.OrderMapper;
import com.lezh1n.goodminton_shop_api.repositories.OrderRepository;
import com.lezh1n.goodminton_shop_api.repositories.PaymentRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductVariantRepository;
import com.lezh1n.goodminton_shop_api.repositories.StoreRepository;
import com.lezh1n.goodminton_shop_api.security.CurrentAccountProvider;
import com.lezh1n.goodminton_shop_api.services.InventoryService;
import com.lezh1n.goodminton_shop_api.services.OrderService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ProductVariantRepository productVariantRepository;
    private final StoreRepository storeRepository;

    private final InventoryService inventoryService;
    private final CurrentAccountProvider currentAccountProvider;
    private final OrderMapper orderMapper;

    private final OrderProperties orderProperties;
    private final VNPayProperties vnpayProperties;
    private final PayOSProperties payOSProperties;

    // ---------- Customer ----------

    @Override
    public OrderResponse createOnlineOrder(CreateOnlineOrderRequest request) {
        Account customer = currentAccountProvider.getCurrentAccount();
        Store central = inventoryService.findCentralStore();
        LocalDateTime now = LocalDateTime.now();

        Order order = Order.builder()
                .customer(customer)
                .store(central)
                .orderType(OrderType.ONLINE)
                .status(OrderStatus.PENDING)
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .recipientAddress(request.getRecipientAddress())
                .recipientEmail(request.getRecipientEmail())
                .note(request.getNote())
                .orderDate(now)
                .totalAmount(BigDecimal.ZERO)
                .orderItems(new ArrayList<>())
                .payments(new ArrayList<>())
                .build();

        BigDecimal total = buildItemsAndDeductStock(order, request.getItems(), central.getId());
        order.setTotalAmount(total);

        // Save order before payment so FK is valid.
        Order saved = orderRepository.save(order);

        // For VNPAY / PAYOS, payment record is created later via the provider's create-payment-url endpoint.
        PaymentMethod method = request.getPaymentMethod();
        if (method != PaymentMethod.VNPAY && method != PaymentMethod.PAYOS) {
            paymentRepository.save(buildPendingPayment(saved, method, total, now));
        }

        return orderMapper.toOrderResponse(reload(saved.getId()));
    }

    @Override
    public OrderResponse getMyOrder(Integer orderId) {
        Order order = loadOrder(orderId);
        ensureOwner(order);
        return orderMapper.toOrderResponse(order);
    }

    @Override
    public Page<OrderResponse> getMyOrders(Pageable pageable) {
        Account customer = currentAccountProvider.getCurrentAccount();
        return orderRepository.findByCustomer_Id(customer.getId(), pageable).map(orderMapper::toOrderResponse);
    }

    @Override
    public OrderResponse cancelMyOrder(Integer orderId) {
        Order order = loadOrder(orderId);
        ensureOwner(order);

        // Customer can only cancel before admin confirms.
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(ErrorCode.ORDER_CANNOT_CANCEL);
        }

        restockItems(order);
        order.setStatus(OrderStatus.CANCELLED);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @Override
    @CacheEvict(value = CacheConfig.RECOMMENDATIONS_CACHE, allEntries = true)
    public OrderResponse confirmReceived(Integer orderId) {
        Order order = loadOrder(orderId);
        ensureOwner(order);
        requireStatus(order, OrderStatus.DELIVERED);
        order.setStatus(OrderStatus.COMPLETED);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    // ---------- Store admin ----------

    @Override
    @CacheEvict(value = CacheConfig.RECOMMENDATIONS_CACHE, allEntries = true)
    public OrderResponse createInStoreOrder(CreateInStoreOrderRequest request) {
        Account adminAccount = currentAccountProvider.getCurrentAccount();
        Store store = storeRepository.findByAdmin_Id(adminAccount.getId())
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        Order order = Order.builder()
                .customer(null)
                .store(store)
                .orderType(OrderType.IN_STORE)
                .status(OrderStatus.COMPLETED)
                .recipientName(request.getCustomerName())
                .recipientPhone(request.getCustomerPhone())
                .orderDate(now)
                .totalAmount(BigDecimal.ZERO)
                .orderItems(new ArrayList<>())
                .payments(new ArrayList<>())
                .build();

        BigDecimal total = buildItemsAndDeductStock(order, request.getItems(), store.getId());
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        // In-store sales are paid at counter — payment is PAID immediately.
        Payment payment = buildPendingPayment(saved, request.getPaymentMethod(), total, now);
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(now);
        paymentRepository.save(payment);

        return orderMapper.toOrderResponse(reload(saved.getId()));
    }

    @Override
    public OrderResponse markPreparing(Integer orderId) {
        return transitionByStoreAdmin(orderId, OrderStatus.CONFIRMED, OrderStatus.PREPARING, null);
    }

    @Override
    public OrderResponse markShipping(Integer orderId, String shippingCode) {
        return transitionByStoreAdmin(orderId, OrderStatus.PREPARING, OrderStatus.SHIPPING, shippingCode);
    }

    @Override
    public OrderResponse markDelivered(Integer orderId) {
        return transitionByStoreAdmin(orderId, OrderStatus.SHIPPING, OrderStatus.DELIVERED, null);
    }

    @Override
    public Page<OrderResponse> getStoreOrders(Pageable pageable) {
        Account admin = currentAccountProvider.getCurrentAccount();
        Store store = storeRepository.findByAdmin_Id(admin.getId())
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        return orderRepository.findByStore_Id(store.getId(), pageable).map(orderMapper::toOrderResponse);
    }

    // ---------- Super admin ----------

    @Override
    public OrderResponse confirmOrder(Integer orderId) {
        Order order = loadOrder(orderId);
        requireStatus(order, OrderStatus.PENDING);

        // Re-assign central store in case it changed since order creation.
        if (order.getStore() == null) {
            order.setStore(inventoryService.findCentralStore());
        }
        order.setStatus(OrderStatus.CONFIRMED);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse getOrderById(Integer orderId) {
        Order order = loadOrder(orderId);
        Account current = currentAccountProvider.getCurrentAccount();
        if (current.getRole() == UserRole.STORE_ADMIN) {
            ensureStoreAdminOwns(order);
        }
        return orderMapper.toOrderResponse(order);
    }

    @Override
    public Page<OrderResponse> getAllOrders(OrderStatus status, OrderType type, Pageable pageable) {
        if (status != null && type != null) {
            return orderRepository.findByOrderTypeAndStatus(type, status, pageable).map(orderMapper::toOrderResponse);
        }
        if (status != null) {
            return orderRepository.findByStatus(status, pageable).map(orderMapper::toOrderResponse);
        }
        return orderRepository.findAll(pageable).map(orderMapper::toOrderResponse);
    }

    // ---------- Scheduled jobs ----------

    @Override
    @CacheEvict(value = CacheConfig.RECOMMENDATIONS_CACHE, allEntries = true)
    public int autoCompleteDeliveredOrders() {
        // Approximation: uses orderDate since Order has no delivered_at column yet (see
        // PERFORMANCE.md).
        LocalDateTime threshold = LocalDateTime.now().minusDays(orderProperties.getAutoCompleteDays());
        List<Order> eligible = orderRepository.findEligibleForAutoComplete(
                OrderStatus.DELIVERED, OrderType.ONLINE, threshold);
        eligible.forEach(o -> o.setStatus(OrderStatus.COMPLETED));
        orderRepository.saveAll(eligible);
        return eligible.size();
    }

    @Override
    public int cancelExpiredProviderPaymentOrders() {
        int timeoutMinutes = Math.max(
                vnpayProperties.getPaymentTimeoutMinutes(),
                payOSProperties.getPaymentTimeoutMinutes());
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(timeoutMinutes);
        List<PaymentMethod> providerMethods = List.of(PaymentMethod.VNPAY, PaymentMethod.PAYOS);
        List<Order> expired = orderRepository.findExpiredProviderPayment(
                OrderStatus.PENDING, providerMethods,
                PaymentStatus.PENDING, PaymentStatus.PAID, threshold);

        for (Order order : expired) {
            restockItems(order);
            order.getPayments().stream()
                    .filter(p -> providerMethods.contains(p.getMethod()) && p.getStatus() == PaymentStatus.PENDING)
                    .forEach(p -> p.setStatus(PaymentStatus.FAILED));
            order.setStatus(OrderStatus.CANCELLED);
        }
        orderRepository.saveAll(expired);
        return expired.size();
    }

    // ---------- Helpers ----------

    private BigDecimal buildItemsAndDeductStock(Order order, List<OrderItemRequest> itemRequests, Integer storeId) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest req : itemRequests) {
            ProductVariant variant = productVariantRepository.findById(req.getVariantId())
                    .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

            // Atomic stock deduction — throws if insufficient.
            inventoryService.deduct(storeId, variant.getId(), req.getQuantity());

            // Snapshot price at order time so later sale_price changes don't affect
            // history.
            BigDecimal unitPrice = variant.getPrice();
            BigDecimal discountPrice = variant.getSalePrice();
            BigDecimal effective = discountPrice != null ? discountPrice : unitPrice;

            order.getOrderItems().add(OrderItem.builder()
                    .order(order)
                    .variant(variant)
                    .quantity(req.getQuantity())
                    .unitPrice(unitPrice)
                    .discountPrice(discountPrice)
                    .build());

            total = total.add(effective.multiply(BigDecimal.valueOf(req.getQuantity())));
        }
        return total;
    }

    private void restockItems(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.restock(order.getStore().getId(), item.getVariant().getId(), item.getQuantity());
        }
    }

    private Payment buildPendingPayment(Order order, PaymentMethod method, BigDecimal amount, LocalDateTime now) {
        return Payment.builder()
                .order(order)
                .method(method)
                .status(PaymentStatus.PENDING)
                .amount(amount)
                .createdAt(now)
                .build();
    }

    private OrderResponse transitionByStoreAdmin(Integer orderId, OrderStatus from, OrderStatus to,
            String shippingCode) {
        Order order = loadOrder(orderId);
        ensureStoreAdminOwns(order);
        requireStatus(order, from);
        order.setStatus(to);
        if (shippingCode != null) {
            order.setShippingCode(shippingCode);
        }
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    private Order loadOrder(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
    }

    private Order reload(Integer orderId) {
        return loadOrder(orderId);
    }

    private void ensureOwner(Order order) {
        Account current = currentAccountProvider.getCurrentAccount();
        if (order.getCustomer() == null || !order.getCustomer().getId().equals(current.getId())) {
            throw new AppException(ErrorCode.ORDER_FORBIDDEN);
        }
    }

    private void ensureStoreAdminOwns(Order order) {
        Account admin = currentAccountProvider.getCurrentAccount();
        Store store = storeRepository.findByAdmin_Id(admin.getId())
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        if (order.getStore() == null || !order.getStore().getId().equals(store.getId())) {
            throw new AppException(ErrorCode.ORDER_FORBIDDEN);
        }
    }

    private void requireStatus(Order order, OrderStatus expected) {
        if (order.getStatus() != expected) {
            throw new AppException(ErrorCode.ORDER_INVALID_STATUS);
        }
    }
}
