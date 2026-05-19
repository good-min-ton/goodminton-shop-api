package com.lezh1n.goodminton_shop_api.events;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.products}")
    private String productsExchange;

    // Only publish after DB commit succeeds — prevents drift when transaction rolls back.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProductChangedEvent event) {
        String routingKey = "product." + event.action();
        try {
            rabbitTemplate.convertAndSend(productsExchange, routingKey, event);
            log.debug("Published {} for product {}", routingKey, event.productId());
        } catch (Exception e) {
            log.error("Failed to publish {} for product {}", routingKey, event.productId(), e);
        }
    }
}
