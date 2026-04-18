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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Appointments", description = "Appointment, diagnosis, and prescription management APIs")
public class OpdController {

    private final OpdService opdService;

    public OpdController(OpdService opdService) {
        this.opdService = opdService;
    }

    @PostMapping("/appointments")
    @Operation(summary = "Create appointment", description = "Creates a new OPD appointment token")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Appointment token generated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(@Valid @RequestBody CreateAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Appointment token generated successfully", opdService.createAppointment(request)));
    }

    @GetMapping("/appointments")
    @Operation(summary = "List appointments", description = "Lists appointments by optional doctor and status filters")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Appointments fetched successfully")
    })
    public ApiResponse<List<AppointmentResponse>> listAppointments(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) AppointmentStatus status
    ) {
        return ApiResponse.success("Appointments fetched successfully", opdService.getAppointments(doctorId, status));
    }

    @GetMapping("/appointments/{id}")
    @Operation(summary = "Get appointment", description = "Fetches appointment details by appointment ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Appointment fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    public ApiResponse<AppointmentResponse> getAppointment(@PathVariable Long id) {
        return ApiResponse.success("Appointment fetched successfully", opdService.getAppointmentById(id));
    }

    @PutMapping("/appointments/{id}/status")
    @Operation(summary = "Update appointment status", description = "Updates an appointment status")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Appointment status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    public ApiResponse<AppointmentResponse> updateAppointmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAppointmentStatusRequest request
    ) {
        return ApiResponse.success("Appointment status updated successfully", opdService.updateAppointmentStatus(id, request.getStatus()));
    }

    @PostMapping("/appointments/{id}/diagnosis")
    @Operation(summary = "Add diagnosis", description = "Adds diagnosis and prescriptions for an appointment")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Diagnosis and prescriptions added successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    public ResponseEntity<ApiResponse<DiagnosisResponse>> addDiagnosis(
            @PathVariable Long id,
            @Valid @RequestBody CreateDiagnosisRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Diagnosis and prescriptions added successfully", opdService.addDiagnosis(id, request)));
    }

    @PutMapping("/prescriptions/{id}/dispense")
    @Operation(summary = "Dispense prescription", description = "Marks prescription as dispensed and updates inventory")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Prescription dispensed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Prescription not found")
    })
    public ApiResponse<PrescriptionResponse> dispensePrescription(@PathVariable Long id) {
        return ApiResponse.success("Prescription dispensed successfully", opdService.dispensePrescription(id));
    }
}
