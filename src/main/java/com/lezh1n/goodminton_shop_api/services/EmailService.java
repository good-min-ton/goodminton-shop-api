package com.lezh1n.goodminton_shop_api.services;

public interface EmailService {
    void sendPasswordResetEmail(String recieveEmail, String token);
}
