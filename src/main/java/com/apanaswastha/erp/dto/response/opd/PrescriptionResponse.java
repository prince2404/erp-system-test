package com.apanaswastha.erp.dto.response.opd;

import java.time.Instant;

public class PrescriptionResponse {

    private final Long id;
    private final Long diagnosisId;
    private final String medicineName;
    private final String dosage;
    private final String duration;
    private final boolean dispensed;
    private final Instant createdAt;

    public PrescriptionResponse(
            Long id,
            Long diagnosisId,
            String medicineName,
            String dosage,
            String duration,
            boolean dispensed,
            Instant createdAt
    ) {
        this.id = id;
        this.diagnosisId = diagnosisId;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.duration = duration;
        this.dispensed = dispensed;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getDiagnosisId() {
        return diagnosisId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public String getDosage() {
        return dosage;
    }

    public String getDuration() {
        return duration;
    }

    public boolean isDispensed() {
        return dispensed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
