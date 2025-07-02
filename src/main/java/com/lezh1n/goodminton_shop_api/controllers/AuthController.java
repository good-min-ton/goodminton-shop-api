package com.lezh1n.goodminton_shop_api.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lezh1n.goodminton_shop_api.dto.ApiResponse;
import com.lezh1n.goodminton_shop_api.dto.request.CreateAccountRequest;
import com.lezh1n.goodminton_shop_api.dto.request.LoginRequest;
import com.lezh1n.goodminton_shop_api.dto.request.LogoutRequest;
import com.lezh1n.goodminton_shop_api.dto.request.RefreshTokenRequest;
import com.lezh1n.goodminton_shop_api.dto.response.AccountResponse;
import com.lezh1n.goodminton_shop_api.dto.response.AuthenticationResponse;
import com.lezh1n.goodminton_shop_api.services.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AccountResponse> register(@RequestBody CreateAccountRequest request) {
        return ApiResponse.<AccountResponse>builder()
                .result(authService.register(request))
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authService.login(request))
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authService.refreshToken(request))
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<String> refreshToken(@RequestBody LogoutRequest request) {
        authService.logout(request);
        return ApiResponse.<String>builder()
                .result("Logout successfully")
                .build();
    }

    @GetMapping("/my-info")
    public ApiResponse<AccountResponse> getMyInfo() {
        return ApiResponse.<AccountResponse>builder()
                .result(authService.getMyInfo())
                .build();
    }
}
