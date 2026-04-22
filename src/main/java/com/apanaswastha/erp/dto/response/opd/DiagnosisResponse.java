package com.apanaswastha.erp.dto.response.opd;

import java.time.Instant;
import java.util.List;

public class DiagnosisResponse {

    private final Long id;
    private final Long appointmentId;
    private final String symptoms;
    private final String medicalNotes;
    private final Instant createdAt;
    private final List<PrescriptionResponse> prescriptions;

    public DiagnosisResponse(
            Long id,
            Long appointmentId,
            String symptoms,
            String medicalNotes,
            Instant createdAt,
            List<PrescriptionResponse> prescriptions
    ) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.symptoms = symptoms;
        this.medicalNotes = medicalNotes;
        this.createdAt = createdAt;
        this.prescriptions = prescriptions;
    }

    public Long getId() {
        return id;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public String getMedicalNotes() {
        return medicalNotes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<PrescriptionResponse> getPrescriptions() {
        return prescriptions;
    }
}
