package com.lezh1n.goodminton_shop_api.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.lezh1n.goodminton_shop_api.enums.AccountStatus;
import com.lezh1n.goodminton_shop_api.enums.UserRole;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new Converter<String, UserRole>() {
            @Override
            public UserRole convert(@NonNull String source) {
                return UserRole.valueOf(source.trim().toUpperCase());
            }
        });

        registry.addConverter(new Converter<String, AccountStatus>() {
            @Override
            public AccountStatus convert(@NonNull String source) {
                return AccountStatus.valueOf(source.trim().toUpperCase());
            }
        });
    }
}
