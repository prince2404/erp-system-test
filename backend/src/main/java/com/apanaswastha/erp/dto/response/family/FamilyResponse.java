package com.apanaswastha.erp.dto.response.family;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class FamilyResponse {

    private final Long id;
    private final String familyHeadName;
    private final String healthCardNumber;
    private final String qrCodeReference;
    private final BigDecimal walletBalance;
    private final Long centerId;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final List<FamilyMemberResponse> members;

    public FamilyResponse(
            Long id,
            String familyHeadName,
            String healthCardNumber,
            String qrCodeReference,
            BigDecimal walletBalance,
            Long centerId,
            Instant createdAt,
            Instant updatedAt,
            List<FamilyMemberResponse> members
    ) {
        this.id = id;
        this.familyHeadName = familyHeadName;
        this.healthCardNumber = healthCardNumber;
        this.qrCodeReference = qrCodeReference;
        this.walletBalance = walletBalance;
        this.centerId = centerId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.members = members;
    }

    public Long getId() {
        return id;
    }

    public String getFamilyHeadName() {
        return familyHeadName;
    }

    public String getHealthCardNumber() {
        return healthCardNumber;
    }

    public String getQrCodeReference() {
        return qrCodeReference;
    }

    public BigDecimal getWalletBalance() {
        return walletBalance;
    }

    public Long getCenterId() {
        return centerId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<FamilyMemberResponse> getMembers() {
        return members;
    }
}
