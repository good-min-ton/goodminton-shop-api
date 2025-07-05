package com.lezh1n.goodminton_shop_api.services;

import org.springframework.data.domain.Page;

import com.lezh1n.goodminton_shop_api.dtos.request.UpdateProfileRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.AccountResponse;

public interface AccountService {
    AccountResponse getAccountById(Integer id);

    Page<AccountResponse> getAllAccounts(int page, int size, String sortBy, String sortDir);

    AccountResponse getMyInfo();

    AccountResponse updateProfile(Integer accountId, UpdateProfileRequest request);
}
