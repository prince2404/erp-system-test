package com.apanaswastha.erp.dto;

import com.apanaswastha.erp.entity.enums.CenterType;

import java.time.Instant;

public class CenterResponse {

    private final Long id;
    private final String name;
    private final String centerCode;
    private final CenterType type;
    private final Long blockId;
    private final String address;
    private final String contactNumber;
    private final Instant createdAt;
    private final Instant updatedAt;

    public CenterResponse(
            Long id,
            String name,
            String centerCode,
            CenterType type,
            Long blockId,
            String address,
            String contactNumber,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.centerCode = centerCode;
        this.type = type;
        this.blockId = blockId;
        this.address = address;
        this.contactNumber = contactNumber;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCenterCode() {
        return centerCode;
    }

    public CenterType getType() {
        return type;
    }

    public Long getBlockId() {
        return blockId;
    }

    public String getAddress() {
        return address;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
