package com.lezh1n.goodminton_shop_api.configurations;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.products}")
    private String productsExchange;

    @Value("${rabbitmq.queue.rag-product-sync}")
    private String ragProductSyncQueue;

    @Value("${rabbitmq.queue.rag-product-sync-dlq}")
    private String ragProductSyncDlq;

    @Bean
    TopicExchange productsExchange() {
        return new TopicExchange(productsExchange, true, false);
    }

    @Bean
    Queue ragProductSyncDlq() {
        return QueueBuilder.durable(ragProductSyncDlq).build();
    }

    @Bean
    Queue ragProductSyncQueue() {
        return QueueBuilder.durable(ragProductSyncQueue)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", ragProductSyncDlq)
                .build();
    }

    @Bean
    Binding ragProductSyncBinding(Queue ragProductSyncQueue, TopicExchange productsExchange) {
        return BindingBuilder.bind(ragProductSyncQueue).to(productsExchange).with("product.*");
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
