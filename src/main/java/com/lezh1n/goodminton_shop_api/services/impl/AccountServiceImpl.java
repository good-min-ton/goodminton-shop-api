package com.lezh1n.goodminton_shop_api.services.impl;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.dtos.request.ChangePasswordRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ForgotPasswordRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ResetPasswordRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.UpdateProfileRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.AccountResponse;
import com.lezh1n.goodminton_shop_api.entities.Account;
import com.lezh1n.goodminton_shop_api.enums.AccountStatus;
import com.lezh1n.goodminton_shop_api.enums.UserRole;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.mappers.AccountMapper;
import com.lezh1n.goodminton_shop_api.repositories.AccountRepository;
import com.lezh1n.goodminton_shop_api.security.CurrentAccountProvider;
import com.lezh1n.goodminton_shop_api.services.AccountService;
import com.lezh1n.goodminton_shop_api.services.EmailService;
import com.lezh1n.goodminton_shop_api.services.TokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;
    private final CurrentAccountProvider currentAccountProvider;
    private final TokenService tokenService;
    private final EmailService emailService;

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public AccountResponse getAccountById(Integer id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        return accountMapper.toAccountResponse(account);
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Page<AccountResponse> getAllAccounts(int page, int size, String sortBy, String sortDir, UserRole role) {
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);

        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<Account> accountPage;
        if (role != null) {
            accountPage = accountRepository.findByRole(role, pageable);
        } else {
            accountPage = accountRepository.findAll(pageable);
        }

        return accountPage.map(accountMapper::toAccountResponse);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public AccountResponse getMyInfo() {
        Account account = currentAccountProvider.getCurrentAccount();

        return accountMapper.toAccountResponse(account);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public AccountResponse updateProfile(UpdateProfileRequest request) {
        Account account = currentAccountProvider.getCurrentAccount();

        account.setFullName(request.getFullName());
        account.setPhone(request.getPhone());

        return accountMapper.toAccountResponse(accountRepository.save(account));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void changePassword(ChangePasswordRequest request) {
        Account account = currentAccountProvider.getCurrentAccount();

        if (!passwordEncoder.matches(request.getOldPassword(), account.getPassword())) {
            throw new AppException(ErrorCode.ACCOUNT_OLD_PASSWORD_NOT_MATCH);
        }

        if (passwordEncoder.matches(request.getNewPassword(), account.getPassword())) {
            throw new AppException(ErrorCode.ACCOUNT_NEW_PASSWORD_SAME_AS_OLD);
        }

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));

        accountRepository.save(account);
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void changeAccountStatus(Integer accountId, AccountStatus status) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (accountId.equals(currentAccountProvider.getCurrentAccountId())) {
            throw new AppException(ErrorCode.ACCOUNT_CANT_BE_LOCKED);
        }

        account.setStatus(status);
        accountRepository.save(account);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        String resetToken = UUID.randomUUID().toString();
        tokenService.addResetToken(resetToken, account.getEmail());
        emailService.sendPasswordResetEmail(account.getEmail(), resetToken);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        String redisKey = "reset_token:" + request.getToken();
        String redisValue = tokenService.getValue(redisKey);

        if (redisValue == null) {
            throw new AppException(ErrorCode.ACCOUNT_RESET_TOKEN_INVALID);
        }

        Account account = accountRepository.findByEmail(redisValue)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (passwordEncoder.matches(request.getNewPassword(), account.getPassword())) {
            throw new AppException(ErrorCode.ACCOUNT_PASSWORD_SAME_AS_OLD);
        }

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
        tokenService.deleteValue(redisKey);
    }
}
