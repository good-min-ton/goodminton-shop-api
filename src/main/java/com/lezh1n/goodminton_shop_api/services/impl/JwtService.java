package com.lezh1n.goodminton_shop_api.services.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.entities.Account;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.services.TokenService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final TokenService tokenService;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String tokenIssuer;

    @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}")
    private String secretKey;

    @Value("${spring.security.oauth2.resourceserver.jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${spring.security.oauth2.resourceserver.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    public String generateAccessToken(Account account) {
        return generateToken(account, accessTokenExpiration, "access_token");
    }

    public String generateRefreshToken(Account account) {
        return generateToken(account, refreshTokenExpiration, "refresh_token");
    }

    private String generateToken(Account account, long expiration, String tokenType) {
        try {
            JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                    .subject(account.getFullName())
                    .issuer(tokenIssuer)
                    .issueTime(new Date(
                            Instant.now().plus(expiration, ChronoUnit.MILLIS).toEpochMilli()))
                    .claim("role", account.getRole())
                    .claim("token_type", tokenType)
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.HS512).build(), jwtClaimsSet);

            signedJWT.sign(new MACSigner(secretKey.getBytes()));
            return signedJWT.serialize();
        } catch (JOSEException e) {
            log.error("Can not create token: ", e);
            throw new AppException(ErrorCode.JWT_GENERATION_ERROR);
        }
    }
}
