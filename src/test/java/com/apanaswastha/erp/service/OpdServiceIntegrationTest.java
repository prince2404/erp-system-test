package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.AppointmentResponse;
import com.apanaswastha.erp.dto.BlockResponse;
import com.apanaswastha.erp.dto.CenterResponse;
import com.apanaswastha.erp.dto.CreateAppointmentRequest;
import com.apanaswastha.erp.dto.CreateBlockRequest;
import com.apanaswastha.erp.dto.CreateCenterRequest;
import com.apanaswastha.erp.dto.CreateDiagnosisRequest;
import com.apanaswastha.erp.dto.CreateDistrictRequest;
import com.apanaswastha.erp.dto.CreateFamilyMemberRequest;
import com.apanaswastha.erp.dto.CreateFamilyRequest;
import com.apanaswastha.erp.dto.CreateStateRequest;
import com.apanaswastha.erp.dto.DiagnosisResponse;
import com.apanaswastha.erp.dto.DistrictResponse;
import com.apanaswastha.erp.dto.FamilyMemberResponse;
import com.apanaswastha.erp.dto.FamilyResponse;
import com.apanaswastha.erp.dto.PrescriptionItemRequest;
import com.apanaswastha.erp.dto.PrescriptionResponse;
import com.apanaswastha.erp.entity.Role;
import com.apanaswastha.erp.entity.User;
import com.apanaswastha.erp.entity.enums.AppointmentStatus;
import com.apanaswastha.erp.entity.enums.RoleName;
import com.apanaswastha.erp.repository.RoleRepository;
import com.apanaswastha.erp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OpdServiceIntegrationTest {

    @Autowired
    private StateService stateService;

    @Autowired
    private DistrictService districtService;

    @Autowired
    private BlockService blockService;

    @Autowired
    private CenterService centerService;

    @Autowired
    private FamilyService familyService;

    @Autowired
    private OpdService opdService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCreateAppointmentAddDiagnosisAndDispensePrescription() {
        CenterResponse center = createCenterHierarchy();
        FamilyMemberResponse patient = createFamilyAndMember(center.getId());
        User doctor = createDoctor();

        CreateAppointmentRequest appointmentRequest = new CreateAppointmentRequest();
        appointmentRequest.setCenterId(center.getId());
        appointmentRequest.setPatientId(patient.getId());
        appointmentRequest.setDoctorId(doctor.getId());
        appointmentRequest.setAppointmentDate(LocalDate.now());

        AppointmentResponse appointment = opdService.createAppointment(appointmentRequest);
        assertEquals("1", appointment.getTokenNumber());
        assertEquals(AppointmentStatus.WAITING, appointment.getStatus());

        assertThrows(IllegalArgumentException.class,
                () -> opdService.updateAppointmentStatus(appointment.getId(), AppointmentStatus.COMPLETED));

        AppointmentResponse inConsultation = opdService.updateAppointmentStatus(
                appointment.getId(),
                AppointmentStatus.IN_CONSULTATION
        );
        assertEquals(AppointmentStatus.IN_CONSULTATION, inConsultation.getStatus());

        CreateDiagnosisRequest diagnosisRequest = new CreateDiagnosisRequest();
        diagnosisRequest.setSymptoms("Fever, headache");
        diagnosisRequest.setMedicalNotes("Likely viral fever");

        PrescriptionItemRequest prescriptionItemRequest = new PrescriptionItemRequest();
        prescriptionItemRequest.setMedicineName("Paracetamol");
        prescriptionItemRequest.setDosage("500mg");
        prescriptionItemRequest.setDuration("3 days");
        diagnosisRequest.setPrescriptions(List.of(prescriptionItemRequest));

        DiagnosisResponse diagnosis = opdService.addDiagnosis(appointment.getId(), diagnosisRequest);
        assertEquals(1, diagnosis.getPrescriptions().size());

        assertThrows(IllegalArgumentException.class, () -> opdService.addDiagnosis(appointment.getId(), diagnosisRequest));

        AppointmentResponse pharmacy = opdService.updateAppointmentStatus(appointment.getId(), AppointmentStatus.PHARMACY);
        assertEquals(AppointmentStatus.PHARMACY, pharmacy.getStatus());

        PrescriptionResponse beforeDispense = diagnosis.getPrescriptions().get(0);
        assertFalse(beforeDispense.isDispensed());

        PrescriptionResponse dispensed = opdService.dispensePrescription(beforeDispense.getId());
        assertTrue(dispensed.isDispensed());
    }

    private User createDoctor() {
        Role doctorRole = roleRepository.findByName(RoleName.DOCTOR)
                .orElseThrow(() -> new IllegalStateException("Doctor role not found"));

        User doctor = new User();
        doctor.setUsername("doctor-opd");
        doctor.setPassword("encoded-password");
        doctor.setEmail("doctor-opd@example.com");
        doctor.setPhone("9999999999");
        doctor.setRole(doctorRole);
        return userRepository.save(doctor);
    }

    private FamilyMemberResponse createFamilyAndMember(Long centerId) {
        CreateFamilyRequest familyRequest = new CreateFamilyRequest();
        familyRequest.setFamilyHeadName("OPD Head");
        familyRequest.setCenterId(centerId);

        FamilyResponse family = familyService.registerFamily(familyRequest);

        CreateFamilyMemberRequest memberRequest = new CreateFamilyMemberRequest();
        memberRequest.setFirstName("Test");
        memberRequest.setLastName("Patient");
        memberRequest.setDob(LocalDate.of(1993, 3, 12));
        memberRequest.setGender("MALE");
        memberRequest.setBloodGroup("O+");

        return familyService.addFamilyMember(family.getHealthCardNumber(), memberRequest);
    }

    private CenterResponse createCenterHierarchy() {
        CreateStateRequest stateRequest = new CreateStateRequest();
        stateRequest.setName("Bihar");
        stateRequest.setCode("BR");
        var state = stateService.create(stateRequest);

        CreateDistrictRequest districtRequest = new CreateDistrictRequest();
        districtRequest.setName("Patna");
        districtRequest.setStateId(state.getId());
        DistrictResponse district = districtService.create(districtRequest);

        CreateBlockRequest blockRequest = new CreateBlockRequest();
        blockRequest.setName("Bihta");
        blockRequest.setDistrictId(district.getId());
        BlockResponse block = blockService.create(blockRequest);

        CreateCenterRequest centerRequest = new CreateCenterRequest();
        centerRequest.setName("OPD Center");
        centerRequest.setCenterCode("BR-PAT-01");
        centerRequest.setAddress("Main Road");
        centerRequest.setContactNumber("9876543210");
        centerRequest.setBlockId(block.getId());

        return centerService.create(centerRequest);
    }
}
