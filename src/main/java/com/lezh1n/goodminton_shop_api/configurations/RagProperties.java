package com.lezh1n.goodminton_shop_api.configurations;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "rag")
public class RagProperties {
    private String baseUrl = "http://rag-service:8000";
    private int timeoutMs = 2000;
    private int retrieveK = 20;
}
