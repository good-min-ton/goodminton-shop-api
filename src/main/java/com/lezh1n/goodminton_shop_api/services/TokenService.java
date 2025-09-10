package com.lezh1n.goodminton_shop_api.services;

public interface TokenService {
    void addToBlacklist(String token, long expiryTime);

    void addResetToken(String token, String value);

    boolean isBlacklisted(String token);

    String getValue(String key);

    void deleteValue(String key);
}
