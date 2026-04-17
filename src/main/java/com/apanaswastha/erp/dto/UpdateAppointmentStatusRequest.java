package com.apanaswastha.erp.dto;

import com.apanaswastha.erp.entity.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateAppointmentStatusRequest {

    @NotNull
    private AppointmentStatus status;

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
}
