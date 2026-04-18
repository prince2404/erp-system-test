package com.apanaswastha.erp.controller;

import com.apanaswastha.erp.dto.AppointmentResponse;
import com.apanaswastha.erp.dto.CreateAppointmentRequest;
import com.apanaswastha.erp.dto.CreateDiagnosisRequest;
import com.apanaswastha.erp.dto.DiagnosisResponse;
import com.apanaswastha.erp.dto.PrescriptionResponse;
import com.apanaswastha.erp.dto.UpdateAppointmentStatusRequest;
import com.apanaswastha.erp.entity.enums.AppointmentStatus;
import com.apanaswastha.erp.payload.ApiResponse;
import com.apanaswastha.erp.service.OpdService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/opd")
public class OpdController {

    private final OpdService opdService;

    public OpdController(OpdService opdService) {
        this.opdService = opdService;
    }

    @PostMapping("/appointments")
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(@Valid @RequestBody CreateAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Appointment token generated successfully", opdService.createAppointment(request)));
    }

    @GetMapping("/appointments")
    public ApiResponse<List<AppointmentResponse>> listAppointments(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) AppointmentStatus status
    ) {
        return ApiResponse.success("Appointments fetched successfully", opdService.getAppointments(doctorId, status));
    }

    @GetMapping("/appointments/{id}")
    public ApiResponse<AppointmentResponse> getAppointment(@PathVariable Long id) {
        return ApiResponse.success("Appointment fetched successfully", opdService.getAppointmentById(id));
    }

    @PutMapping("/appointments/{id}/status")
    public ApiResponse<AppointmentResponse> updateAppointmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAppointmentStatusRequest request
    ) {
        return ApiResponse.success("Appointment status updated successfully", opdService.updateAppointmentStatus(id, request.getStatus()));
    }

    @PostMapping("/appointments/{id}/diagnosis")
    public ResponseEntity<ApiResponse<DiagnosisResponse>> addDiagnosis(
            @PathVariable Long id,
            @Valid @RequestBody CreateDiagnosisRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Diagnosis and prescriptions added successfully", opdService.addDiagnosis(id, request)));
    }

    @PutMapping("/prescriptions/{id}/dispense")
    public ApiResponse<PrescriptionResponse> dispensePrescription(@PathVariable Long id) {
        return ApiResponse.success("Prescription dispensed successfully", opdService.dispensePrescription(id));
    }
}
