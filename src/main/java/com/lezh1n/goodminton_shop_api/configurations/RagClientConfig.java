package com.lezh1n.goodminton_shop_api.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RagClientConfig {

    @Bean
    RestClient ragRestClient(RagProperties ragProperties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(ragProperties.getTimeoutMs());
        factory.setReadTimeout(ragProperties.getTimeoutMs());
        return RestClient.builder()
                .baseUrl(ragProperties.getBaseUrl())
                .requestFactory(factory)
                .build();
    }
}
