package com.lezh1n.goodminton_shop_api.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lezh1n.goodminton_shop_api.dtos.response.NotificationResponse;
import com.lezh1n.goodminton_shop_api.entities.Account;
import com.lezh1n.goodminton_shop_api.entities.Notification;
import com.lezh1n.goodminton_shop_api.entities.Order;
import com.lezh1n.goodminton_shop_api.enums.NotificationType;
import com.lezh1n.goodminton_shop_api.enums.OrderStatus;
import com.lezh1n.goodminton_shop_api.enums.UserRole;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.repositories.AccountRepository;
import com.lezh1n.goodminton_shop_api.repositories.NotificationRepository;
import com.lezh1n.goodminton_shop_api.repositories.OrderRepository;
import com.lezh1n.goodminton_shop_api.security.CurrentAccountProvider;
import com.lezh1n.goodminton_shop_api.services.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Tells the next person in the chain that it is their turn.
 *
 * <p>An order changes hands three times - SUPER_ADMIN confirms, STORE_ADMIN
 * prepares/ships/delivers, CUSTOMER confirms receipt - and every handoff was
 * silent, so orders sat unnoticed. Each status therefore notifies two audiences
 * at most: whoever must act next, and the customer when something visible to
 * them happened.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final CurrentAccountProvider currentAccountProvider;
    private final NotificationStream stream;

    @Override
    @Transactional
    public void notifyStatusChange(Integer orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.warn("Order {} vanished before it could be notified about", orderId);
            return;
        }

        List<Notification> raised = new ArrayList<>();
        switch (status) {
            // Nobody owns a new order yet, so this goes to every super admin
            // rather than one of them - the alternative is picking arbitrarily
            // and hoping that person is on shift.
            case PENDING -> raised.addAll(forEachSuperAdmin(order,
                    NotificationType.ORDER_AWAITING_CONFIRMATION,
                    "Đơn hàng #%d đang chờ xác nhận".formatted(orderId)));

            case CONFIRMED -> {
                raised.addAll(forStoreAdmin(order, NotificationType.ORDER_CONFIRMED,
                        "Đơn hàng #%d đã xác nhận, cần chuẩn bị hàng".formatted(orderId)));
                raised.addAll(forCustomer(order, NotificationType.ORDER_CONFIRMED,
                        "Đơn hàng #%d đã được xác nhận".formatted(orderId)));
            }

            case PREPARING -> raised.addAll(forCustomer(order, NotificationType.ORDER_PREPARING,
                    "Shop đang chuẩn bị đơn hàng #%d".formatted(orderId)));

            // The tracking code goes in the message so a customer can quote it
            // without opening the order.
            case SHIPPING -> raised.addAll(forCustomer(order, NotificationType.ORDER_SHIPPING,
                    order.getShippingCode() == null
                            ? "Đơn hàng #%d đang được giao".formatted(orderId)
                            : "Đơn hàng #%d đang được giao, mã vận đơn %s"
                                    .formatted(orderId, order.getShippingCode())));

            case DELIVERED -> raised.addAll(forCustomer(order, NotificationType.ORDER_DELIVERED,
                    "Đơn hàng #%d đã giao. Bạn xác nhận đã nhận hàng giúp shop nhé"
                            .formatted(orderId)));

            // Only the store: the customer just confirmed receipt themselves, so
            // telling them what they did is noise. It is what clears the order
            // off the store's queue.
            case COMPLETED -> raised.addAll(forStoreAdmin(order, NotificationType.ORDER_COMPLETED,
                    "Đơn hàng #%d đã hoàn tất".formatted(orderId)));

            // Both sides of the shop need this: stock has just gone back. So
            // does the customer - an order can be cancelled without them asking,
            // by the payment-timeout scheduler, and being told is the whole
            // point of a notification.
            case CANCELLED -> {
                raised.addAll(forEachSuperAdmin(order, NotificationType.ORDER_CANCELLED,
                        "Đơn hàng #%d đã bị huỷ".formatted(orderId)));
                raised.addAll(forStoreAdmin(order, NotificationType.ORDER_CANCELLED,
                        "Đơn hàng #%d đã bị huỷ".formatted(orderId)));
                raised.addAll(forCustomer(order, NotificationType.ORDER_CANCELLED,
                        "Đơn hàng #%d của bạn đã bị huỷ".formatted(orderId)));
            }

            default -> {
                // No audience for the remaining statuses.
            }
        }

        if (raised.isEmpty()) {
            return;
        }
        notificationRepository.saveAll(raised);
        // Push after the rows exist, so a client that reacts by refetching sees
        // them. Best-effort: the bell also polls.
        raised.forEach(n -> stream.push(n.getRecipient().getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(Pageable pageable) {
        Integer me = currentAccountProvider.getCurrentAccount().getId();
        return notificationRepository
                .findByRecipient_IdOrderByCreatedAtDesc(me, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long countMyUnread() {
        return notificationRepository
                .countByRecipient_IdAndReadAtIsNull(currentAccountProvider.getCurrentAccount().getId());
    }

    @Override
    @Transactional
    public void markRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));
        // Ownership check: ids are sequential, so without it anyone could clear
        // somebody else's bell by guessing.
        Integer me = currentAccountProvider.getCurrentAccount().getId();
        if (!notification.getRecipient().getId().equals(me)) {
            throw new AppException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public int markAllRead() {
        Integer me = currentAccountProvider.getCurrentAccount().getId();
        return notificationRepository.markAllRead(me, LocalDateTime.now());
    }

    // ---------- Recipients ----------

    private List<Notification> forEachSuperAdmin(Order order, NotificationType type, String message) {
        return accountRepository.findByRole(UserRole.SUPER_ADMIN).stream()
                .map(admin -> build(admin, order, type, message))
                .toList();
    }

    /** The admin of the store fulfilling this order, if it has one. */
    private List<Notification> forStoreAdmin(Order order, NotificationType type, String message) {
        if (order.getStore() == null || order.getStore().getAdmin() == null) {
            return List.of();
        }
        return List.of(build(order.getStore().getAdmin(), order, type, message));
    }

    /** Walk-in orders have no customer account, so this is often empty. */
    private List<Notification> forCustomer(Order order, NotificationType type, String message) {
        if (order.getCustomer() == null) {
            return List.of();
        }
        return List.of(build(order.getCustomer(), order, type, message));
    }

    private Notification build(Account recipient, Order order, NotificationType type, String message) {
        return Notification.builder()
                .recipient(recipient)
                .order(order)
                .type(type)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .orderId(n.getOrder().getId())
                .type(n.getType())
                .message(n.getMessage())
                .read(n.getReadAt() != null)
                .createdAt(n.getCreatedAt())
                .build();
    }
}
