package com.apanaswastha.erp.dto;

import com.apanaswastha.erp.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public class WalletTransactionResponse {

    private final Long id;
    private final Long familyId;
    private final TransactionType transactionType;
    private final BigDecimal amount;
    private final String referenceId;
    private final String description;
    private final Instant createdAt;

    public WalletTransactionResponse(
            Long id,
            Long familyId,
            TransactionType transactionType,
            BigDecimal amount,
            String referenceId,
            String description,
            Instant createdAt
    ) {
        this.id = id;
        this.familyId = familyId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.referenceId = referenceId;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getFamilyId() {
        return familyId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
