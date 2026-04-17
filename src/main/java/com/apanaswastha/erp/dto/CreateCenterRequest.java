package com.apanaswastha.erp.dto;

import com.apanaswastha.erp.entity.enums.CenterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateCenterRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String centerCode;

    private CenterType type;

    @NotNull
    private Long blockId;

    @NotBlank
    private String address;

    @NotBlank
    private String contactNumber;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCenterCode() {
        return centerCode;
    }

    public void setCenterCode(String centerCode) {
        this.centerCode = centerCode;
    }

    public Long getBlockId() {
        return blockId;
    }

    public void setBlockId(Long blockId) {
        this.blockId = blockId;
    }

    public CenterType getType() {
        return type;
    }

    public void setType(CenterType type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}
