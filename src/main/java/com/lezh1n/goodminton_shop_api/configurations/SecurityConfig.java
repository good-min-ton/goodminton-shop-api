package com.lezh1n.goodminton_shop_api.configurations;

import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.lezh1n.goodminton_shop_api.services.impl.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

    private final JwtBlacklistFilter jwtBlacklistFilter;
    private final CustomUserDetailsService userDetailsService;
    private final CustomJwtAuthenticationConverter customConverter;

    private static final String[] POST_PUBLIC_ENDPOINTS = {
            "/api/auth/**",
            "/api/accounts/forgot-password",
            "/api/accounts/reset-password",
            "/api/payos/webhook",
    };

    private static final String[] GET_PUBLIC_ENDPOINTS = {
            "/api/accounts/validate-reset-token",
            "/api/stores", "/api/stores/{storeId}",
            "/api/categories", "/api/categories/{categoryId}",
            "/api/brands", "/api/brands/{brandId}",

            "/api/sizes", "/api/sizes/{sizeId}",
            "api/colors", "api/colors/{colorId}",
            "api/products", "api/products/{productId}",
            "api/products/list-items",
            "api/products/{productId}/recommendations",
            "/api/reviews/{productId}",
            "/api/search/products", "/api/search/products/suggest",
            "/api/search/categories", "/api/search/brands",

            "/api/vnpay/ipn",

            "/v3/api-docs", "/v3/api-docs/**", "/v3/api-docs.yaml",
            "/swagger-ui.html", "/swagger-ui/**",

            "/actuator/health", "/actuator/health/**",
    };

    @Value("${spring.jwt.secret}")
    private String jwtSecret;

    @Value("${app.cors-allowed-origins:*}")
    private String corsAllowedOrigins;

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Origin *patterns*, not plain origins: an entry may carry a "*" so that
        // Vercel preview deployments, whose subdomain changes per branch, are
        // covered without listing each one. Defaults to "*" so an unset value
        // behaves exactly as before rather than locking the frontend out.
        configuration.setAllowedOriginPatterns(Arrays.stream(corsAllowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList());

        configuration.addAllowedMethod("*");
        // Left open: the frontend sends Authorization, and
        // ngrok-skip-browser-warning to get past the tunnel's interstitial.
        configuration.addAllowedHeader("*");
        // Auth is a bearer token the frontend attaches itself, never a cookie, so
        // no credentialed request is made. Enabling this would also make the "*"
        // default illegal - browsers reject that combination.
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);

        authBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/internal/**").permitAll()
                        .requestMatchers(HttpMethod.POST, POST_PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, GET_PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtBlacklistFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(customConverter))
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));
        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(
                new SecretKeySpec(jwtSecret.getBytes(), "HS512"))
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }
}
