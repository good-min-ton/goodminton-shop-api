package com.lezh1n.goodminton_shop_api.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    @Query("SELECT a FROM Account a WHERE a.email = :identifier OR a.phone = :identifier")
    Optional<Account> findByEmailOrPhone(@Param("identifier") String identifier);
}
