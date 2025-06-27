package com.lezh1n.goodminton_shop_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);
}
