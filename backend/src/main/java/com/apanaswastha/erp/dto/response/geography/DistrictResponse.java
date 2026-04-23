package com.apanaswastha.erp.dto.response.geography;

import java.time.Instant;

public class DistrictResponse {

    private final Long id;
    private final String name;
    private final Long stateId;
    private final Instant createdAt;
    private final Instant updatedAt;

    public DistrictResponse(Long id, String name, Long stateId, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.stateId = stateId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getStateId() {
        return stateId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
