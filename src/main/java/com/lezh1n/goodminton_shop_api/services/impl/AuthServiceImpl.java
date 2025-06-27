package com.lezh1n.goodminton_shop_api.services.impl;

import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.dto.request.CreateAccountRequest;
import com.lezh1n.goodminton_shop_api.dto.response.AccountResponse;
import com.lezh1n.goodminton_shop_api.entities.Account;
import com.lezh1n.goodminton_shop_api.enums.UserRole;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.mappers.AccountMapper;
import com.lezh1n.goodminton_shop_api.repositories.AccountRepository;
import com.lezh1n.goodminton_shop_api.services.AuthService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Override
    public AccountResponse register(CreateAccountRequest request) {

        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        if (accountRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.PHONE_EXISTED);
        }

        Account account = accountMapper.toAccount(request);
        account.setRole(UserRole.CUSTOMER);
        return accountMapper.toAccountResponse(accountRepository.save(account));
    }

}
