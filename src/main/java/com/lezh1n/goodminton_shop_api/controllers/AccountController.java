package com.lezh1n.goodminton_shop_api.controllers;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lezh1n.goodminton_shop_api.dtos.ApiResponse;
import com.lezh1n.goodminton_shop_api.dtos.request.CreateAccountRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.AccountResponse;
import com.lezh1n.goodminton_shop_api.enums.UserRole;
import com.lezh1n.goodminton_shop_api.services.AccountService;
import com.lezh1n.goodminton_shop_api.services.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AuthService authService;

    @GetMapping("/{id}")
    public ApiResponse<AccountResponse> getAccountById(@PathVariable Integer id) {
        return ApiResponse.<AccountResponse>builder()
                .result(accountService.getAccountById(id))
                .build();
    }

    @GetMapping
    public ApiResponse<Page<AccountResponse>> getAllAccounts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Page<AccountResponse> accountPage = accountService.getAllAccounts(page, size, sortBy, sortDir);
        return ApiResponse.<Page<AccountResponse>>builder()
                .result(accountPage)
                .build();
    }

    @PostMapping("/store-admin")
    public ApiResponse<AccountResponse> createStoreAdmin(@RequestBody CreateAccountRequest request) {
        return ApiResponse.<AccountResponse>builder()
                .result(authService.register(request, UserRole.STORE_ADMIN))
                .build();
    }
}
