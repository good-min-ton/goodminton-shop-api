package com.lezh1n.goodminton_shop_api.services.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.services.TokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenRedisServiceImpl implements TokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "token_blacklist:";

    @Override
    public void addToBlacklist(String token, long expiryTime) {
        String key = BLACKLIST_PREFIX + token;
        long ttl = expiryTime / 1000;
        if (ttl > 0) {
            redisTemplate.opsForValue().set(key, "blacklisted", ttl, TimeUnit.SECONDS);
            log.info("Token blacklisted with TTL: {} second", ttl);
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        Boolean existed = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(existed);
    }

}
