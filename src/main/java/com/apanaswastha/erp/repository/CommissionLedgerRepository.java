package com.apanaswastha.erp.repository;

import com.apanaswastha.erp.entity.CommissionLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommissionLedgerRepository extends JpaRepository<CommissionLedger, Long> {

    List<CommissionLedger> findByRecipientUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByInvoiceId(Long invoiceId);
}
