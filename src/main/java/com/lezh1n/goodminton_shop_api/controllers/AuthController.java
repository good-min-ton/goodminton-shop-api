package com.lezh1n.goodminton_shop_api.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lezh1n.goodminton_shop_api.dto.ApiResponse;
import com.lezh1n.goodminton_shop_api.dto.request.CreateAccountRequest;
import com.lezh1n.goodminton_shop_api.dto.response.AccountResponse;
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
}
