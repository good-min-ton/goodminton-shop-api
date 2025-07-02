package com.lezh1n.goodminton_shop_api.services;

import com.lezh1n.goodminton_shop_api.dto.request.CreateAccountRequest;
import com.lezh1n.goodminton_shop_api.dto.request.LoginRequest;
import com.lezh1n.goodminton_shop_api.dto.request.LogoutRequest;
import com.lezh1n.goodminton_shop_api.dto.request.RefreshTokenRequest;
import com.lezh1n.goodminton_shop_api.dto.response.AccountResponse;
import com.lezh1n.goodminton_shop_api.dto.response.AuthenticationResponse;

public interface AuthService {
    AccountResponse register(CreateAccountRequest request);

    AuthenticationResponse login(LoginRequest request);

    AuthenticationResponse refreshToken(RefreshTokenRequest request);

    void logout(LogoutRequest request);

    AccountResponse getMyInfo();
}
