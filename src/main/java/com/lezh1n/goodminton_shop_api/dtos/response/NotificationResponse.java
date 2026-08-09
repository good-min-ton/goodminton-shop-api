package com.lezh1n.goodminton_shop_api.dtos.response;

import java.time.LocalDateTime;

import com.lezh1n.goodminton_shop_api.enums.NotificationType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private Integer orderId;
    private NotificationType type;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
}
