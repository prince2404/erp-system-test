package com.apanaswastha.erp.dto.response.commission;

import com.apanaswastha.erp.enums.CommissionStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class CommissionLedgerResponse {

    private final Long id;
    private final Long invoiceId;
    private final Long recipientUserId;
    private final Long roleId;
    private final BigDecimal amount;
    private final BigDecimal percentageApplied;
    private final CommissionStatus status;
    private final Instant createdAt;

    public CommissionLedgerResponse(
            Long id,
            Long invoiceId,
            Long recipientUserId,
            Long roleId,
            BigDecimal amount,
            BigDecimal percentageApplied,
            CommissionStatus status,
            Instant createdAt
    ) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.recipientUserId = recipientUserId;
        this.roleId = roleId;
        this.amount = amount;
        this.percentageApplied = percentageApplied;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public Long getRecipientUserId() {
        return recipientUserId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getPercentageApplied() {
        return percentageApplied;
    }

    public CommissionStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
