package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.request.opd.CreateAppointmentRequest;
import com.apanaswastha.erp.dto.request.opd.CreateDiagnosisRequest;
import com.apanaswastha.erp.dto.response.opd.AppointmentResponse;
import com.apanaswastha.erp.dto.response.opd.DiagnosisResponse;
import com.apanaswastha.erp.dto.response.opd.PrescriptionResponse;
import com.apanaswastha.erp.enums.AppointmentStatus;

import java.util.List;

public interface OpdService {

    /**
     * Creates an OPD appointment.
     *
     * @param request appointment payload
     * @return appointment details
     */
    AppointmentResponse createAppointment(CreateAppointmentRequest request);

    /**
     * Lists appointments by optional filters.
     *
     * @param doctorId doctor id filter
     * @param status status filter
     * @return appointment list
     */
    List<AppointmentResponse> getAppointments(Long doctorId, AppointmentStatus status);

    /**
     * Fetches appointment details.
     *
     * @param appointmentId appointment id
     * @return appointment details
     */
    AppointmentResponse getAppointmentById(Long appointmentId);

    /**
     * Updates appointment status.
     *
     * @param appointmentId appointment id
     * @param targetStatus target status
     * @return updated appointment
     */
    AppointmentResponse updateAppointmentStatus(Long appointmentId, AppointmentStatus targetStatus);

    /**
     * Adds diagnosis and prescription details to appointment.
     *
     * @param appointmentId appointment id
     * @param request diagnosis payload
     * @return diagnosis details
     */
    DiagnosisResponse addDiagnosis(Long appointmentId, CreateDiagnosisRequest request);

    /**
     * Dispenses a prescription.
     *
     * @param prescriptionId prescription id
     * @return dispensed prescription details
     */
    PrescriptionResponse dispensePrescription(Long prescriptionId);
}
