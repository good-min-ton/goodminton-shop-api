package com.lezh1n.goodminton_shop_api.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.entities.Account;
import com.lezh1n.goodminton_shop_api.enums.AccountStatus;
import com.lezh1n.goodminton_shop_api.enums.UserRole;
import com.lezh1n.goodminton_shop_api.repositories.AccountRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.phone}")
    private String adminPhone;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (accountRepository.existsByRole(UserRole.SUPER_ADMIN))
            return;

        Account admin = Account.builder()
                .fullName("Super Admin")
                .email(adminEmail)
                .phone(adminPhone)
                .password(passwordEncoder.encode(adminPassword))
                .role(UserRole.SUPER_ADMIN)
                .status(AccountStatus.ACTIVE)
                .build();

        accountRepository.save(admin);
    }
}
