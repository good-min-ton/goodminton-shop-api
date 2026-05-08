package com.lezh1n.goodminton_shop_api.mappers;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.request.CreateAccountRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.AccountResponse;
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
                .createdAt(LocalDateTime.now())
                .build();
    }

    public AccountResponse toAccountResponse(Account account) {
        if (account == null) {
            return null;
        }
        return AccountResponse.builder()
                .id(account.getId())
                .fullName(account.getFullName())
                .email(account.getEmail())
                .phone(account.getPhone())
                .role(account.getRole())
                .createdAt(account.getCreatedAt())
                .status(account.getStatus())
                .build();
    }
}
