package com.lezh1n.goodminton_shop_api.services.impl;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.services.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderScheduler {

    private final OrderService orderService;

    // Daily at 02:00 — auto-complete delivered online orders.
    @Scheduled(cron = "0 0 2 * * *")
    public void autoCompleteDeliveredOrders() {
        try {
            int count = orderService.autoCompleteDeliveredOrders();
            if (count > 0) {
                log.info("Auto-completed {} delivered online orders", count);
            }
        } catch (Exception e) {
            log.error("autoCompleteDeliveredOrders failed: {}", e.getMessage(), e);
        }
    }

    // Every 5 minutes — cancel expired external-provider (VNPay / PayOS) pending orders and restock.
    @Scheduled(fixedDelay = 5 * 60 * 1000L, initialDelay = 30 * 1000L)
    public void cancelExpiredProviderPaymentOrders() {
        try {
            int count = orderService.cancelExpiredProviderPaymentOrders();
            if (count > 0) {
                log.info("Cancelled {} expired provider-payment orders", count);
            }
        } catch (Exception e) {
            log.error("cancelExpiredProviderPaymentOrders failed: {}", e.getMessage(), e);
        }
    }
}
