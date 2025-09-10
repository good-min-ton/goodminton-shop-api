package com.lezh1n.goodminton_shop_api.services;

import org.springframework.data.domain.Page;

import com.lezh1n.goodminton_shop_api.dtos.request.ChangePasswordRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ForgotPasswordRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ResetPasswordRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.UpdateProfileRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.AccountResponse;

public interface AccountService {
    AccountResponse getAccountById(Integer id);

    Page<AccountResponse> getAllAccounts(int page, int size, String sortBy, String sortDir);

    AccountResponse getMyInfo();

    AccountResponse updateProfile(UpdateProfileRequest request);

    void changePassword(ChangePasswordRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
