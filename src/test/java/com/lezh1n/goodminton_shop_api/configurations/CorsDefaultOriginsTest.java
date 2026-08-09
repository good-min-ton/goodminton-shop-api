package com.lezh1n.goodminton_shop_api.configurations;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

/** An unset app.cors-allowed-origins must behave exactly as before the setting
 *  existed, so adding it cannot lock the frontend out of an existing deploy. */
@SpringBootTest
class CorsDefaultOriginsTest {

    @Autowired
    @Qualifier("corsConfigurationSource")
    CorsConfigurationSource source;

    @Test
    @DisplayName("an unset value keeps every origin allowed")
    void unsetAllowsEverything() {
        CorsConfiguration config = source
                .getCorsConfiguration(new MockHttpServletRequest("GET", "/api/products"));

        assertThat(config.getAllowedOriginPatterns()).isEqualTo(List.of("*"));
        assertThat(config.checkOrigin("https://anything.example.com")).isNotNull();
    }
}
