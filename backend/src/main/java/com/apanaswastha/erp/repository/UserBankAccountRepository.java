package com.apanaswastha.erp.repository;

import com.apanaswastha.erp.entity.UserBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBankAccountRepository extends JpaRepository<UserBankAccount, Long> {
    List<UserBankAccount> findByUserIdAndDeletedFalse(Long userId);
    Optional<UserBankAccount> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);
    Optional<UserBankAccount> findByUserIdAndPrimaryTrueAndDeletedFalse(Long userId);
}
