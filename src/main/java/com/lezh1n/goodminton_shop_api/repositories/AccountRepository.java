package com.lezh1n.goodminton_shop_api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.Account;
import com.lezh1n.goodminton_shop_api.enums.UserRole;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByRole(UserRole role);

    @Query("SELECT a FROM Account a WHERE a.email = :identifier OR a.phone = :identifier")
    Optional<Account> findByEmailOrPhone(@Param("identifier") String identifier);

    Optional<Account> findByEmail(String email);

    Page<Account> findByRole(UserRole role, Pageable pageable);

    @Query(value = """
            SELECT EXISTS (SELECT 1
                FROM accounts
                WHERE id = :accountId
                AND role = 'STORE_ADMIN')
            """, nativeQuery = true)
    boolean isStoreAdminAccount(@Param("accountId") Integer accountId);

    @Query(value = """
            SELECT * FROM accounts a
            WHERE a.role = 'STORE_ADMIN'
            AND a.id NOT IN (SELECT s.admin_id FROM stores s)
            """, nativeQuery = true)
    List<Account> findAdminsNotAssigned();
}
