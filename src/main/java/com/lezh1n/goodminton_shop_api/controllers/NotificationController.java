package com.lezh1n.goodminton_shop_api.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.lezh1n.goodminton_shop_api.common.ApiResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.NotificationResponse;
import com.lezh1n.goodminton_shop_api.security.CurrentAccountProvider;
import com.lezh1n.goodminton_shop_api.services.NotificationService;
import com.lezh1n.goodminton_shop_api.services.impl.NotificationStream;

import lombok.RequiredArgsConstructor;

/** Every signed-in role has a bell: the chain runs customer -> store -> super admin. */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private static final int MAX_PAGE_SIZE = 50;

    private final NotificationService notificationService;
    private final NotificationStream stream;
    private final CurrentAccountProvider currentAccountProvider;

    @GetMapping
    public ApiResponse<Page<NotificationResponse>> getMine(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.min(size, MAX_PAGE_SIZE));
        return ApiResponse.<Page<NotificationResponse>>builder()
                .result(notificationService.getMyNotifications(pageable))
                .build();
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount() {
        return ApiResponse.<Long>builder().result(notificationService.countMyUnread()).build();
    }

    @PostMapping("/{notificationId}/read")
    public ApiResponse<Void> markRead(@PathVariable Long notificationId) {
        notificationService.markRead(notificationId);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/read-all")
    public ApiResponse<Integer> markAllRead() {
        return ApiResponse.<Integer>builder().result(notificationService.markAllRead()).build();
    }

    /**
     * Nudges the client to refetch. Carries no payload: the list and the badge
     * stay the single source of truth, and the bell polls anyway, so a tunnel
     * that buffers or drops this stream costs immediacy and nothing else.
     */
    @GetMapping("/stream")
    public SseEmitter stream() {
        return stream.subscribe(currentAccountProvider.getCurrentAccount().getId());
    }
}
