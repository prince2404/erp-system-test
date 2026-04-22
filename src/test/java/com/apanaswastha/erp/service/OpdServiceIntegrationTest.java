package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.response.opd.AppointmentResponse;
import com.apanaswastha.erp.dto.response.geography.BlockResponse;
import com.apanaswastha.erp.dto.response.geography.CenterResponse;
import com.apanaswastha.erp.dto.request.opd.CreateAppointmentRequest;
import com.apanaswastha.erp.dto.request.geography.CreateBlockRequest;
import com.apanaswastha.erp.dto.request.geography.CreateCenterRequest;
import com.apanaswastha.erp.dto.request.opd.CreateDiagnosisRequest;
import com.apanaswastha.erp.dto.request.geography.CreateDistrictRequest;
import com.apanaswastha.erp.dto.request.family.CreateFamilyMemberRequest;
import com.apanaswastha.erp.dto.request.family.CreateFamilyRequest;
import com.apanaswastha.erp.dto.request.inventory.CreateInventoryBatchRequest;
import com.apanaswastha.erp.dto.request.inventory.CreateMedicineRequest;
import com.apanaswastha.erp.dto.request.geography.CreateStateRequest;
import com.apanaswastha.erp.dto.response.opd.DiagnosisResponse;
import com.apanaswastha.erp.dto.response.geography.DistrictResponse;
import com.apanaswastha.erp.dto.response.family.FamilyMemberResponse;
import com.apanaswastha.erp.dto.response.family.FamilyResponse;
import com.apanaswastha.erp.dto.request.opd.PrescriptionItemRequest;
import com.apanaswastha.erp.dto.response.opd.PrescriptionResponse;
import com.apanaswastha.erp.entity.Vendor;
import com.apanaswastha.erp.entity.Role;
import com.apanaswastha.erp.entity.User;
import com.apanaswastha.erp.enums.AppointmentStatus;
import com.apanaswastha.erp.enums.RoleName;
import com.apanaswastha.erp.repository.RoleRepository;
import com.apanaswastha.erp.repository.UserRepository;
import com.apanaswastha.erp.repository.VendorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.math.BigDecimal;
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
    private InventoryService inventoryService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VendorRepository vendorRepository;

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

        seedInventory(center.getId(), "Paracetamol");

        PrescriptionResponse beforeDispense = diagnosis.getPrescriptions().get(0);
        assertFalse(beforeDispense.isDispensed());

        PrescriptionResponse dispensed = opdService.dispensePrescription(beforeDispense.getId());
        assertTrue(dispensed.isDispensed());
    }

    @Test
    void shouldAllowConsultationCompletionToBillingAfterDiagnosis() {
        CenterResponse center = createCenterHierarchy();
        FamilyMemberResponse patient = createFamilyAndMember(center.getId());
        User doctor = createDoctor();

        CreateAppointmentRequest appointmentRequest = new CreateAppointmentRequest();
        appointmentRequest.setCenterId(center.getId());
        appointmentRequest.setPatientId(patient.getId());
        appointmentRequest.setDoctorId(doctor.getId());
        appointmentRequest.setAppointmentDate(LocalDate.now());
        appointmentRequest.setChiefComplaint("High fever since yesterday");

        AppointmentResponse appointment = opdService.createAppointment(appointmentRequest);
        assertEquals("High fever since yesterday", appointment.getChiefComplaint());

        opdService.updateAppointmentStatus(appointment.getId(), AppointmentStatus.IN_CONSULTATION);

        CreateDiagnosisRequest diagnosisRequest = new CreateDiagnosisRequest();
        diagnosisRequest.setSymptoms("High fever");
        diagnosisRequest.setMedicalNotes("Need hydration and monitoring");

        PrescriptionItemRequest prescriptionItemRequest = new PrescriptionItemRequest();
        prescriptionItemRequest.setMedicineName("Paracetamol");
        prescriptionItemRequest.setDosage("500mg");
        prescriptionItemRequest.setDuration("2 days");
        diagnosisRequest.setPrescriptions(List.of(prescriptionItemRequest));

        opdService.addDiagnosis(appointment.getId(), diagnosisRequest);

        AppointmentResponse billing = opdService.updateAppointmentStatus(appointment.getId(), AppointmentStatus.BILLING);
        assertEquals(AppointmentStatus.BILLING, billing.getStatus());
    }

    private void seedInventory(Long centerId, String medicineName) {
        CreateMedicineRequest medicineRequest = new CreateMedicineRequest();
        medicineRequest.setName(medicineName);
        medicineRequest.setGenericName("Acetaminophen");
        medicineRequest.setManufacturer("ABC Pharma");
        Long medicineId = inventoryService.addMedicine(medicineRequest).getId();

        Vendor vendor = new Vendor();
        vendor.setName("MedSupply");
        vendor.setContactInfo("9876501234");
        vendor.setAddress("Patna");
        vendor = vendorRepository.save(vendor);

        CreateInventoryBatchRequest batchRequest = new CreateInventoryBatchRequest();
        batchRequest.setMedicineId(medicineId);
        batchRequest.setVendorId(vendor.getId());
        batchRequest.setCenterId(centerId);
        batchRequest.setBatchNumber("PARA-1");
        batchRequest.setExpiryDate(LocalDate.now().plusDays(120));
        batchRequest.setQuantityReceived(20);
        batchRequest.setUnitPrice(new BigDecimal("5.00"));
        batchRequest.setSellingPrice(new BigDecimal("6.50"));
        inventoryService.addBatch(batchRequest);
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
