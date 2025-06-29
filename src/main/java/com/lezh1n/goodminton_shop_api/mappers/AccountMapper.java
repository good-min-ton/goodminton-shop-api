package com.lezh1n.goodminton_shop_api.mappers;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dto.request.CreateAccountRequest;
import com.lezh1n.goodminton_shop_api.dto.response.AccountResponse;
import com.lezh1n.goodminton_shop_api.entities.Account;
import com.lezh1n.goodminton_shop_api.enums.AccountStatus;

@Component
public class AccountMapper {

    public Account toAccount(CreateAccountRequest request) {
        if (request == null) {
            return null;
        }
        return Account.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .status(AccountStatus.ACTIVE)
                .build();
    }

    public AccountResponse toAccountResponse(Account account) {
        if (account == null) {
            return null;
        }
        return AccountResponse.builder()
                .accountId(account.getAccountId())
                .fullName(account.getFullName())
                .email(account.getEmail())
                .phone(account.getPhone())
                .password(account.getPassword())
                .role(account.getRole())
                .status(account.getStatus())
                .build();
    }
}
