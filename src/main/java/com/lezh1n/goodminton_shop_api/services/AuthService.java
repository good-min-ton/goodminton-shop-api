package com.lezh1n.goodminton_shop_api.services;

import com.lezh1n.goodminton_shop_api.dtos.request.CreateAccountRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.LoginRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.LogoutRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.RefreshTokenRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.AccountResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.AuthenticationResponse;

public interface AuthService {
    AccountResponse register(CreateAccountRequest request);

    AuthenticationResponse login(LoginRequest request);

    AuthenticationResponse refreshToken(RefreshTokenRequest request);

    void logout(LogoutRequest request);

    AccountResponse getMyInfo();
}
