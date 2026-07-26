package com.lezh1n.goodminton_shop_api.configurations;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "recommendations")
public class RecommendationProperties {
    private double boostBestseller = 0.03;
    private double boostSale = 0.02;
}
