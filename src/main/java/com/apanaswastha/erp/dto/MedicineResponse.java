package com.apanaswastha.erp.dto;

import java.time.Instant;

public class MedicineResponse {

    private final Long id;
    private final String name;
    private final String genericName;
    private final String manufacturer;
    private final Instant createdAt;
    private final Instant updatedAt;

    public MedicineResponse(Long id, String name, String genericName, String manufacturer, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.genericName = genericName;
        this.manufacturer = manufacturer;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGenericName() {
        return genericName;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
