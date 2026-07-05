package com.lezh1n.goodminton_shop_api.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import vn.payos.PayOS;
import vn.payos.core.ClientOptions;

@Configuration
@RequiredArgsConstructor
public class PayOSConfig {

    private final PayOSProperties props;

    @Bean
    PayOS payOS() {
        ClientOptions options = ClientOptions.builder()
                .clientId(props.getClientId())
                .apiKey(props.getApiKey())
                .checksumKey(props.getChecksumKey())
                .build();
        return new PayOS(options);
    }
}
