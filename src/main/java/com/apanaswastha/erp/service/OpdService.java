package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.AppointmentResponse;
import com.apanaswastha.erp.dto.CreateAppointmentRequest;
import com.apanaswastha.erp.dto.CreateDiagnosisRequest;
import com.apanaswastha.erp.dto.DiagnosisResponse;
import com.apanaswastha.erp.dto.PrescriptionItemRequest;
import com.apanaswastha.erp.dto.PrescriptionResponse;
import com.apanaswastha.erp.entity.Appointment;
import com.apanaswastha.erp.entity.Center;
import com.apanaswastha.erp.entity.Diagnosis;
import com.apanaswastha.erp.entity.FamilyMember;
import com.apanaswastha.erp.entity.Prescription;
import com.apanaswastha.erp.entity.User;
import com.apanaswastha.erp.entity.enums.AppointmentStatus;
import com.apanaswastha.erp.entity.enums.RoleName;
import com.apanaswastha.erp.exception.NotFoundException;
import com.apanaswastha.erp.repository.AppointmentRepository;
import com.apanaswastha.erp.repository.CenterRepository;
import com.apanaswastha.erp.repository.DiagnosisRepository;
import com.apanaswastha.erp.repository.FamilyMemberRepository;
import com.apanaswastha.erp.repository.PrescriptionRepository;
import com.apanaswastha.erp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@Service
public class OpdService {

    private static final Map<AppointmentStatus, EnumSet<AppointmentStatus>> ALLOWED_TRANSITIONS = Map.of(
            AppointmentStatus.WAITING, EnumSet.of(AppointmentStatus.IN_CONSULTATION),
            AppointmentStatus.IN_CONSULTATION, EnumSet.of(AppointmentStatus.PHARMACY),
            AppointmentStatus.PHARMACY, EnumSet.of(AppointmentStatus.BILLING),
            AppointmentStatus.BILLING, EnumSet.of(AppointmentStatus.COMPLETED),
            AppointmentStatus.COMPLETED, EnumSet.noneOf(AppointmentStatus.class)
    );

    private final AppointmentRepository appointmentRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final CenterRepository centerRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;

    public OpdService(
            AppointmentRepository appointmentRepository,
            DiagnosisRepository diagnosisRepository,
            PrescriptionRepository prescriptionRepository,
            FamilyMemberRepository familyMemberRepository,
            CenterRepository centerRepository,
            UserRepository userRepository,
            InventoryService inventoryService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.familyMemberRepository = familyMemberRepository;
        this.centerRepository = centerRepository;
        this.userRepository = userRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        FamilyMember patient = familyMemberRepository.findById(request.getPatientId())
                .orElseThrow(() -> new NotFoundException("Patient not found with id: " + request.getPatientId()));

        Center center = centerRepository.findById(request.getCenterId())
                .orElseThrow(() -> new NotFoundException("Center not found with id: " + request.getCenterId()));

        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new NotFoundException("Doctor not found with id: " + request.getDoctorId()));

        if (doctor.getRole() == null || doctor.getRole().getName() != RoleName.DOCTOR) {
            throw new IllegalArgumentException("Selected user is not a doctor");
        }

        long tokenSequence = appointmentRepository.countByCenterIdAndAppointmentDate(center.getId(), request.getAppointmentDate()) + 1;

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setCenter(center);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setStatus(AppointmentStatus.WAITING);
        appointment.setTokenNumber(String.valueOf(tokenSequence));

        return toAppointmentResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse updateAppointmentStatus(Long appointmentId, AppointmentStatus targetStatus) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found with id: " + appointmentId));

        AppointmentStatus currentStatus = appointment.getStatus();
        if (currentStatus == targetStatus) {
            return toAppointmentResponse(appointment);
        }

        if (!ALLOWED_TRANSITIONS.getOrDefault(currentStatus, EnumSet.noneOf(AppointmentStatus.class)).contains(targetStatus)) {
            throw new IllegalArgumentException("Invalid status transition from " + currentStatus + " to " + targetStatus);
        }

        if (EnumSet.of(AppointmentStatus.PHARMACY, AppointmentStatus.BILLING, AppointmentStatus.COMPLETED).contains(targetStatus)
                && !diagnosisRepository.existsByAppointmentId(appointmentId)) {
            throw new IllegalArgumentException("Diagnosis is required before moving appointment to " + targetStatus);
        }

        appointment.setStatus(targetStatus);
        return toAppointmentResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public DiagnosisResponse addDiagnosis(Long appointmentId, CreateDiagnosisRequest request) {
        if (diagnosisRepository.existsByAppointmentId(appointmentId)) {
            throw new IllegalArgumentException("Diagnosis is immutable and already exists for appointment id: " + appointmentId);
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found with id: " + appointmentId));

        if (appointment.getStatus() != AppointmentStatus.IN_CONSULTATION) {
            throw new IllegalArgumentException("Diagnosis can only be added when appointment is IN_CONSULTATION");
        }

        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setAppointment(appointment);
        diagnosis.setSymptoms(request.getSymptoms());
        diagnosis.setMedicalNotes(request.getMedicalNotes());
        Diagnosis savedDiagnosis = diagnosisRepository.save(diagnosis);

        List<PrescriptionResponse> prescriptions = request.getPrescriptions().stream()
                .map(item -> createPrescription(savedDiagnosis, item))
                .map(this::toPrescriptionResponse)
                .toList();

        return new DiagnosisResponse(
                savedDiagnosis.getId(),
                appointment.getId(),
                savedDiagnosis.getSymptoms(),
                savedDiagnosis.getMedicalNotes(),
                savedDiagnosis.getCreatedAt(),
                prescriptions
        );
    }

    @Transactional
    public PrescriptionResponse dispensePrescription(Long prescriptionId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new NotFoundException("Prescription not found with id: " + prescriptionId));

        if (!prescription.isDispensed()) {
            Long centerId = prescription.getDiagnosis().getAppointment().getCenter().getId();
            inventoryService.dispenseMedicine(centerId, prescription.getMedicineName(), 1);
            prescription.setDispensed(true);
            prescription = prescriptionRepository.save(prescription);
        }

        return toPrescriptionResponse(prescription);
    }

    private Prescription createPrescription(Diagnosis diagnosis, PrescriptionItemRequest item) {
        Prescription prescription = new Prescription();
        prescription.setDiagnosis(diagnosis);
        prescription.setMedicineName(item.getMedicineName());
        prescription.setDosage(item.getDosage());
        prescription.setDuration(item.getDuration());
        return prescriptionRepository.save(prescription);
    }

    private AppointmentResponse toAppointmentResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getCenter().getId(),
                appointment.getDoctor().getId(),
                appointment.getTokenNumber(),
                appointment.getStatus(),
                appointment.getAppointmentDate(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }

    private PrescriptionResponse toPrescriptionResponse(Prescription prescription) {
        return new PrescriptionResponse(
                prescription.getId(),
                prescription.getDiagnosis().getId(),
                prescription.getMedicineName(),
                prescription.getDosage(),
                prescription.getDuration(),
                prescription.isDispensed(),
                prescription.getCreatedAt()
        );
    }
}
