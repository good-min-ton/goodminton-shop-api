package com.lezh1n.goodminton_shop_api.configurations;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "order")
public class OrderProperties {
    private int autoCompleteDays = 7;
}
