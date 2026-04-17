package com.apanaswastha.erp.dto;

import java.time.Instant;

public class StateResponse {

    private final Long id;
    private final String name;
    private final String code;
    private final Instant createdAt;
    private final Instant updatedAt;

    public StateResponse(Long id, String name, String code, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
