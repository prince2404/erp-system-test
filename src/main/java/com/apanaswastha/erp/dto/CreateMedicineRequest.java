package com.apanaswastha.erp.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateMedicineRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String genericName;

    @NotBlank
    private String manufacturer;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
}
