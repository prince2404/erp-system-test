package com.apanaswastha.erp.dto.request.opd;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class CreateDiagnosisRequest {

    @NotBlank
    private String symptoms;

    @NotBlank
    private String medicalNotes;

    @Valid
    @NotEmpty
    private List<PrescriptionItemRequest> prescriptions;

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getMedicalNotes() {
        return medicalNotes;
    }

    public void setMedicalNotes(String medicalNotes) {
        this.medicalNotes = medicalNotes;
    }

    public List<PrescriptionItemRequest> getPrescriptions() {
        return prescriptions;
    }

    public void setPrescriptions(List<PrescriptionItemRequest> prescriptions) {
        this.prescriptions = prescriptions;
    }
}
