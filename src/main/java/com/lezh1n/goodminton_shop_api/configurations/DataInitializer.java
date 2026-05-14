package com.lezh1n.goodminton_shop_api.configurations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.lezh1n.goodminton_shop_api.entities.Account;
import com.lezh1n.goodminton_shop_api.entities.Inventory;
import com.lezh1n.goodminton_shop_api.entities.Store;
import com.lezh1n.goodminton_shop_api.enums.AccountStatus;
import com.lezh1n.goodminton_shop_api.enums.UserRole;
import com.lezh1n.goodminton_shop_api.repositories.AccountRepository;
import com.lezh1n.goodminton_shop_api.repositories.InventoryRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductVariantRepository;
import com.lezh1n.goodminton_shop_api.repositories.StoreRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final BigDecimal HQ_LONGITUDE = new BigDecimal("106.7665");
    private static final BigDecimal HQ_LATITUDE = new BigDecimal("10.9080");
    private static final int INVENTORY_MIN = 20;
    private static final int INVENTORY_MAX = 30;
    private static final Random RNG = new Random();

    private final AccountRepository accountRepository;
    private final StoreRepository storeRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductVariantRepository variantRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.phone}")
    private String adminPhone;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.store-admin.email}")
    private String storeAdminEmail;

    @Value("${app.store-admin.phone}")
    private String storeAdminPhone;

    @Value("${app.store-admin.password}")
    private String storeAdminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensureSuperAdmin();
        Account storeAdmin = ensureStoreAdmin();
        Store hq = ensureHqStore(storeAdmin);
        ensureInventory(hq);
    }

    private void ensureSuperAdmin() {
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

    private Account ensureStoreAdmin() {
        return accountRepository.findByEmail(storeAdminEmail).orElseGet(() -> {
            Account admin = Account.builder()
                    .fullName("HQ Store Admin")
                    .email(storeAdminEmail)
                    .phone(storeAdminPhone)
                    .password(passwordEncoder.encode(storeAdminPassword))
                    .role(UserRole.STORE_ADMIN)
                    .status(AccountStatus.ACTIVE)
                    .build();
            return accountRepository.save(admin);
        });
    }

    private Store ensureHqStore(Account admin) {
        return storeRepository.findByIsCentralTrue().orElseGet(() -> {
            Store hq = Store.builder()
                    .admin(admin)
                    .name("Goodminton HQ - Di An")
                    .address("Di An, Ho Chi Minh")
                    .contact(storeAdminPhone)
                    .longitude(HQ_LONGITUDE)
                    .latitude(HQ_LATITUDE)
                    .isCentral(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            return storeRepository.save(hq);
        });
    }

    private void ensureInventory(Store store) {
        if (inventoryRepository.existsByStore_Id(store.getId()))
            return;

        LocalDateTime now = LocalDateTime.now();
        var rows = variantRepository.findAll().stream()
                .map(v -> Inventory.builder()
                        .store(store)
                        .variant(v)
                        .quantity(RNG.nextInt(INVENTORY_MAX - INVENTORY_MIN + 1) + INVENTORY_MIN)
                        .updatedAt(now)
                        .build())
                .toList();
        inventoryRepository.saveAll(rows);
    }
}
