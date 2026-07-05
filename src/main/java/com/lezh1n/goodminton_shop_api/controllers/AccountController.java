package com.lezh1n.goodminton_shop_api.controllers;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lezh1n.goodminton_shop_api.common.ApiResponse;
import com.lezh1n.goodminton_shop_api.dtos.request.ChangePasswordRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.CreateAccountRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ForgotPasswordRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ResetPasswordRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.UpdateProfileRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.AccountResponse;
import com.lezh1n.goodminton_shop_api.enums.AccountStatus;
import com.lezh1n.goodminton_shop_api.enums.UserRole;
import com.lezh1n.goodminton_shop_api.services.AccountService;
import com.lezh1n.goodminton_shop_api.services.AuthService;

import jakarta.validation.Valid;
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
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Page<AccountResponse>> getAllAccounts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) UserRole role) {

        Page<AccountResponse> accountPage = accountService.getAllAccounts(page, size, sortBy, sortDir, role);
        return ApiResponse.<Page<AccountResponse>>builder()
                .result(accountPage)
                .build();
    }

    @PostMapping("/store-admin")
    public ApiResponse<AccountResponse> createStoreAdmin(@Valid @RequestBody CreateAccountRequest request) {
        return ApiResponse.<AccountResponse>builder()
                .result(authService.register(request, UserRole.STORE_ADMIN))
                .build();
    }

    @GetMapping("/my-info")
    public ApiResponse<AccountResponse> getMyInfo() {
        return ApiResponse.<AccountResponse>builder()
                .result(accountService.getMyInfo())
                .build();
    }

    @PutMapping("/my-info")
    public ApiResponse<AccountResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.<AccountResponse>builder()
                .result(accountService.updateProfile(request))
                .build();
    }

    @PatchMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        accountService.changePassword(request);
        return ApiResponse.<Void>builder().build();
    }

    @PatchMapping("/{accountId}/status/{status}")
    public ApiResponse<Void> changeAccountStatus(@PathVariable Integer accountId,
            @PathVariable AccountStatus status) {
        accountService.changeAccountStatus(accountId, status);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        accountService.forgotPassword(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        accountService.resetPassword(request);
        return ApiResponse.<Void>builder().build();
    }
}
