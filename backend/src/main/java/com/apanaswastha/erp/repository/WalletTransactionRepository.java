package com.apanaswastha.erp.repository;

import com.apanaswastha.erp.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    boolean existsByReferenceId(String referenceId);

    List<WalletTransaction> findAllByFamilyIdOrderByCreatedAtDesc(Long familyId);
}
