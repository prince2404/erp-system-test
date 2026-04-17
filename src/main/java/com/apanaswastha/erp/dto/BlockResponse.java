package com.apanaswastha.erp.dto;

import java.time.Instant;

public class BlockResponse {

    private final Long id;
    private final String name;
    private final Long districtId;
    private final Instant createdAt;
    private final Instant updatedAt;

    public BlockResponse(Long id, String name, Long districtId, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.districtId = districtId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getDistrictId() {
        return districtId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
