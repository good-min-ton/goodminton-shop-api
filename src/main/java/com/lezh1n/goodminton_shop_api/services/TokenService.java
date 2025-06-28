package com.lezh1n.goodminton_shop_api.services;

public interface TokenService {
    public void addToBlacklist(String token, long expiryTime);
    public boolean isBlacklisted(String token);
}
