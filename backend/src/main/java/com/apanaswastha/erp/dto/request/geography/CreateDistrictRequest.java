package com.apanaswastha.erp.dto.request.geography;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateDistrictRequest {

    @NotBlank
    private String name;

    @NotNull
    private Long stateId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getStateId() {
        return stateId;
    }

    public void setStateId(Long stateId) {
        this.stateId = stateId;
    }
}
