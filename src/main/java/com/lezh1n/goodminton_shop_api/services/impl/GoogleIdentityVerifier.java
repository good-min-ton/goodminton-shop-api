package com.lezh1n.goodminton_shop_api.services.impl;

import java.util.List;
import java.util.Set;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.configurations.GoogleProperties;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Verifies the ID token that Google hands the browser after a successful
 * sign-in.
 *
 * That token is an ordinary RS256 JWT, so the resource-server support already on
 * the classpath decodes it and no Google client library is needed.
 * NimbusJwtDecoder fetches Google's JWKS, caches it, and re-fetches on an
 * unknown key id, which is what makes Google's key rotation a non-event here.
 *
 * A valid signature proves only that Google issued the token, not that it was
 * issued for us. Checking the audience is what stops a token minted for another
 * site's client id from being replayed against this API, so it is checked
 * explicitly rather than left to a default.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleIdentityVerifier {

    private static final String JWK_SET_URI = "https://www.googleapis.com/oauth2/v3/certs";

    /** Google mints tokens under both spellings; both are legitimate. */
    private static final Set<String> ISSUERS = Set.of(
            "accounts.google.com",
            "https://accounts.google.com");

    private final GoogleProperties googleProperties;

    private volatile JwtDecoder decoder;

    /**
     * Built lazily so the application still starts when Google sign-in is not
     * configured: the decoder reaches out to the network on first use, and a
     * deployment that never calls this endpoint should not depend on that.
     */
    private JwtDecoder decoder() {
        JwtDecoder local = decoder;
        if (local == null) {
            synchronized (this) {
                local = decoder;
                if (local == null) {
                    local = NimbusJwtDecoder.withJwkSetUri(JWK_SET_URI).build();
                    decoder = local;
                }
            }
        }
        return local;
    }

    /**
     * @return the verified claims
     * @throws AppException if the token is missing, unreadable, not from Google,
     *                      or not addressed to this application
     */
    public Jwt verify(String credential) {
        if (!googleProperties.isEnabled()) {
            throw new AppException(ErrorCode.AUTH_GOOGLE_DISABLED);
        }
        if (credential == null || credential.isBlank()) {
            throw new AppException(ErrorCode.AUTH_GOOGLE_TOKEN_INVALID);
        }

        Jwt jwt;
        try {
            // Decoding already enforces the signature and the expiry.
            jwt = decoder().decode(credential);
        } catch (JwtException e) {
            // Deliberately not passed back to the caller: why a token failed is
            // useful to someone probing the endpoint and useless to a customer,
            // who can only try again.
            log.warn("Rejected a Google sign-in: {}", e.getMessage());
            throw new AppException(ErrorCode.AUTH_GOOGLE_TOKEN_INVALID);
        }

        String issuer = jwt.getIssuer() == null ? "" : jwt.getIssuer().toString();
        if (!ISSUERS.contains(issuer)) {
            log.warn("Google sign-in carried an unexpected issuer: {}", issuer);
            throw new AppException(ErrorCode.AUTH_GOOGLE_TOKEN_INVALID);
        }

        List<String> audience = jwt.getAudience();
        if (audience == null || !audience.contains(googleProperties.getClientId())) {
            log.warn("Google sign-in was issued for another audience: {}", audience);
            throw new AppException(ErrorCode.AUTH_GOOGLE_TOKEN_INVALID);
        }

        return jwt;
    }
}
