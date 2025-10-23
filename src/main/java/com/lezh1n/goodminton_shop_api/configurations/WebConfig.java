package com.lezh1n.goodminton_shop_api.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(@NonNull FormatterRegistry registry) {
        registry.addConverterFactory(new StringToEnumIgnoreCaseConverterFactory());
    }

    @SuppressWarnings("rawtypes")
    private static class StringToEnumIgnoreCaseConverterFactory implements ConverterFactory<String, Enum> {

        @Override
        @NonNull
        public <T extends Enum> Converter<String, T> getConverter(@NonNull Class<T> targetType) {
            return new StringToEnumConverter<>(targetType);
        }

        @RequiredArgsConstructor
        private static class StringToEnumConverter<T extends Enum<T>> implements Converter<String, T> {
            private final Class<T> enumType;

            @Override
            @Nullable
            public T convert(@NonNull String source) {
                if (source.trim().isEmpty()) {
                    return null;
                }
                return Enum.valueOf(enumType, source.trim().toUpperCase());
            }

        }

    }
}
