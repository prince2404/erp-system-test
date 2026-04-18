package com.apanaswastha.erp.dto;

import com.apanaswastha.erp.entity.enums.AppointmentStatus;

import java.time.Instant;
import java.time.LocalDate;

public class AppointmentResponse {

    private final Long id;
    private final Long patientId;
    private final Long centerId;
    private final Long doctorId;
    private final String tokenNumber;
    private final AppointmentStatus status;
    private final LocalDate appointmentDate;
    private final String chiefComplaint;
    private final Instant createdAt;
    private final Instant updatedAt;

    public AppointmentResponse(
            Long id,
            Long patientId,
            Long centerId,
            Long doctorId,
            String tokenNumber,
            AppointmentStatus status,
            LocalDate appointmentDate,
            String chiefComplaint,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.patientId = patientId;
        this.centerId = centerId;
        this.doctorId = doctorId;
        this.tokenNumber = tokenNumber;
        this.status = status;
        this.appointmentDate = appointmentDate;
        this.chiefComplaint = chiefComplaint;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public Long getCenterId() {
        return centerId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public String getTokenNumber() {
        return tokenNumber;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
