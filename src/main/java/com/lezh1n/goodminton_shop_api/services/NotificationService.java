package com.lezh1n.goodminton_shop_api.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.lezh1n.goodminton_shop_api.dtos.response.NotificationResponse;
import com.lezh1n.goodminton_shop_api.enums.OrderStatus;

public interface NotificationService {

    /** Raise notifications for whoever has to act on this status. */
    void notifyStatusChange(Integer orderId, OrderStatus status);

    Page<NotificationResponse> getMyNotifications(Pageable pageable);

    long countMyUnread();

    void markRead(Long notificationId);

    int markAllRead();
}
