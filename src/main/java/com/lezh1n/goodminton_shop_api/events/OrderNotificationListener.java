package com.lezh1n.goodminton_shop_api.events;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.lezh1n.goodminton_shop_api.services.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Turns a status change into notifications for whoever has to act next.
 *
 * <p>AFTER_COMMIT, mirroring ProductEventPublisher: a rolled-back transaction
 * must not leave someone told about an order that no longer exists in that
 * state. REQUIRES_NEW because the original transaction is already finished by
 * the time this runs, so the writes need one of their own.
 *
 * <p>Failures are logged, never rethrown. A notification is an aid, not part of
 * the order: losing one must never fail the request that placed or advanced it.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderNotificationListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(OrderStatusChangedEvent event) {
        try {
            notificationService.notifyStatusChange(event.orderId(), event.status());
        } catch (Exception e) {
            log.error("Failed to notify for order {} -> {}",
                    event.orderId(), event.status(), e);
        }
    }
}
