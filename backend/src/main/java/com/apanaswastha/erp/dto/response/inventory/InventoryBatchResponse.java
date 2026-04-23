package com.apanaswastha.erp.dto.response.inventory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class InventoryBatchResponse {

    private final Long id;
    private final Long medicineId;
    private final String medicineName;
    private final Long vendorId;
    private final Long centerId;
    private final String batchNumber;
    private final LocalDate expiryDate;
    private final Integer quantityReceived;
    private final Integer quantityAvailable;
    private final BigDecimal unitPrice;
    private final BigDecimal sellingPrice;
    private final Instant createdAt;

    public InventoryBatchResponse(
            Long id,
            Long medicineId,
            String medicineName,
            Long vendorId,
            Long centerId,
            String batchNumber,
            LocalDate expiryDate,
            Integer quantityReceived,
            Integer quantityAvailable,
            BigDecimal unitPrice,
            BigDecimal sellingPrice,
            Instant createdAt
    ) {
        this.id = id;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.vendorId = vendorId;
        this.centerId = centerId;
        this.batchNumber = batchNumber;
        this.expiryDate = expiryDate;
        this.quantityReceived = quantityReceived;
        this.quantityAvailable = quantityAvailable;
        this.unitPrice = unitPrice;
        this.sellingPrice = sellingPrice;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getMedicineId() {
        return medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public Long getCenterId() {
        return centerId;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public Integer getQuantityReceived() {
        return quantityReceived;
    }

    public Integer getQuantityAvailable() {
        return quantityAvailable;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
