package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.AppointmentResponse;
import com.apanaswastha.erp.dto.BlockResponse;
import com.apanaswastha.erp.dto.CenterResponse;
import com.apanaswastha.erp.dto.CommissionLedgerResponse;
import com.apanaswastha.erp.dto.CreateAppointmentRequest;
import com.apanaswastha.erp.dto.CreateBlockRequest;
import com.apanaswastha.erp.dto.CreateCenterRequest;
import com.apanaswastha.erp.dto.CreateDiagnosisRequest;
import com.apanaswastha.erp.dto.CreateDistrictRequest;
import com.apanaswastha.erp.dto.CreateFamilyMemberRequest;
import com.apanaswastha.erp.dto.CreateFamilyRequest;
import com.apanaswastha.erp.dto.CreateInventoryBatchRequest;
import com.apanaswastha.erp.dto.CreateMedicineRequest;
import com.apanaswastha.erp.dto.CreateStateRequest;
import com.apanaswastha.erp.dto.DistrictResponse;
import com.apanaswastha.erp.dto.FamilyMemberResponse;
import com.apanaswastha.erp.dto.FamilyResponse;
import com.apanaswastha.erp.dto.InvoiceResponse;
import com.apanaswastha.erp.dto.PrescriptionItemRequest;
import com.apanaswastha.erp.dto.PrescriptionResponse;
import com.apanaswastha.erp.dto.WalletTransactionRequest;
import com.apanaswastha.erp.entity.Role;
import com.apanaswastha.erp.entity.User;
import com.apanaswastha.erp.entity.Vendor;
import com.apanaswastha.erp.entity.enums.AppointmentStatus;
import com.apanaswastha.erp.entity.enums.PaymentMethod;
import com.apanaswastha.erp.entity.enums.PaymentStatus;
import com.apanaswastha.erp.entity.enums.RoleName;
import com.apanaswastha.erp.exception.InsufficientBalanceException;
import com.apanaswastha.erp.repository.RoleRepository;
import com.apanaswastha.erp.repository.UserRepository;
import com.apanaswastha.erp.repository.VendorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BillingCommissionIntegrationTest {

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
    private BillingService billingService;

    @Autowired
    private CommissionService commissionService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Test
    void shouldGenerateInvoicePayByWalletAndDistributeCommissions() {
        CenterResponse center = createCenterHierarchy("BILL-OK");
        FamilyResponse family = createFamily(center.getId(), "Billing Family");
        FamilyMemberResponse member = createFamilyMember(family.getHealthCardNumber());

        User associate = createUser(RoleName.ASSOCIATE, "assoc-bill");
        User blockManager = createUser(RoleName.BLOCK_MANAGER, "block-bill");
        User districtManager = createUser(RoleName.DISTRICT_MANAGER, "district-bill");
        User stateManager = createUser(RoleName.STATE_MANAGER, "state-bill");
        User superAdmin = createUser(RoleName.SUPER_ADMIN, "super-bill");
        User doctor = createUser(RoleName.DOCTOR, "doctor-bill");

        seedInventory(center.getId(), "Paracetamol", new BigDecimal("10.00"));

        AppointmentResponse billingAppointment = createAppointmentAtBilling(member.getId(), center.getId(), doctor.getId());

        InvoiceResponse invoice = billingService.generateInvoice(billingAppointment.getId());
        assertEquals(PaymentStatus.PENDING, invoice.getPaymentStatus());
        assertEquals(new BigDecimal("110.00"), invoice.getTotalAmount());
        assertEquals(2, invoice.getItems().size());

        WalletTransactionRequest creditRequest = new WalletTransactionRequest();
        creditRequest.setHealthCardNumber(family.getHealthCardNumber());
        creditRequest.setAmount(new BigDecimal("200.00"));
        creditRequest.setReferenceId("wallet-credit-billing-ok");
        creditRequest.setDescription("Wallet top-up for invoice");
        walletService.credit(creditRequest);

        InvoiceResponse paidInvoice = billingService.processPayment(invoice.getId(), PaymentMethod.WALLET);
        assertEquals(PaymentStatus.PAID, paidInvoice.getPaymentStatus());
        assertEquals(PaymentMethod.WALLET, paidInvoice.getPaymentMethod());

        FamilyResponse updatedFamily = familyService.getByHealthCardNumber(family.getHealthCardNumber());
        assertEquals(new BigDecimal("90.00"), updatedFamily.getWalletBalance());

        List<CommissionLedgerResponse> associateCommissions = commissionService.getUserCommissions(associate.getId());
        assertEquals(1, associateCommissions.size());
        assertEquals(new BigDecimal("4.40"), associateCommissions.get(0).getAmount());

        List<CommissionLedgerResponse> blockManagerCommissions = commissionService.getUserCommissions(blockManager.getId());
        assertEquals(1, blockManagerCommissions.size());
        assertEquals(new BigDecimal("3.30"), blockManagerCommissions.get(0).getAmount());

        List<CommissionLedgerResponse> districtManagerCommissions = commissionService.getUserCommissions(districtManager.getId());
        assertEquals(1, districtManagerCommissions.size());
        assertEquals(new BigDecimal("2.20"), districtManagerCommissions.get(0).getAmount());

        List<CommissionLedgerResponse> stateManagerCommissions = commissionService.getUserCommissions(stateManager.getId());
        assertEquals(1, stateManagerCommissions.size());
        assertEquals(new BigDecimal("1.10"), stateManagerCommissions.get(0).getAmount());

        List<CommissionLedgerResponse> superAdminCommissions = commissionService.getUserCommissions(superAdmin.getId());
        assertEquals(1, superAdminCommissions.size());
        assertEquals(new BigDecimal("0.55"), superAdminCommissions.get(0).getAmount());

        assertThrows(IllegalArgumentException.class,
                () -> billingService.processPayment(invoice.getId(), PaymentMethod.CASH));
    }

    @Test
    void shouldRejectWalletPaymentWhenBalanceIsInsufficient() {
        CenterResponse center = createCenterHierarchy("BILL-NO");
        FamilyResponse family = createFamily(center.getId(), "Billing Family 2");
        FamilyMemberResponse member = createFamilyMember(family.getHealthCardNumber());
        User doctor = createUser(RoleName.DOCTOR, "doctor-bill2");

        seedInventory(center.getId(), "Paracetamol", new BigDecimal("10.00"));

        AppointmentResponse billingAppointment = createAppointmentAtBilling(member.getId(), center.getId(), doctor.getId());
        InvoiceResponse invoice = billingService.generateInvoice(billingAppointment.getId());

        assertThrows(InsufficientBalanceException.class,
                () -> billingService.processPayment(invoice.getId(), PaymentMethod.WALLET));

        InvoiceResponse reloaded = billingService.getInvoice(invoice.getId());
        assertEquals(PaymentStatus.PENDING, reloaded.getPaymentStatus());
    }

    private AppointmentResponse createAppointmentAtBilling(Long memberId, Long centerId, Long doctorId) {
        CreateAppointmentRequest appointmentRequest = new CreateAppointmentRequest();
        appointmentRequest.setCenterId(centerId);
        appointmentRequest.setPatientId(memberId);
        appointmentRequest.setDoctorId(doctorId);
        appointmentRequest.setAppointmentDate(LocalDate.now());
        AppointmentResponse appointment = opdService.createAppointment(appointmentRequest);

        opdService.updateAppointmentStatus(appointment.getId(), AppointmentStatus.IN_CONSULTATION);

        CreateDiagnosisRequest diagnosisRequest = new CreateDiagnosisRequest();
        diagnosisRequest.setSymptoms("Fever");
        diagnosisRequest.setMedicalNotes("Viral");

        PrescriptionItemRequest prescriptionItemRequest = new PrescriptionItemRequest();
        prescriptionItemRequest.setMedicineName("Paracetamol");
        prescriptionItemRequest.setDosage("500mg");
        prescriptionItemRequest.setDuration("3 days");
        diagnosisRequest.setPrescriptions(List.of(prescriptionItemRequest));

        var diagnosis = opdService.addDiagnosis(appointment.getId(), diagnosisRequest);
        opdService.updateAppointmentStatus(appointment.getId(), AppointmentStatus.PHARMACY);

        PrescriptionResponse prescription = diagnosis.getPrescriptions().get(0);
        opdService.dispensePrescription(prescription.getId());

        return opdService.updateAppointmentStatus(appointment.getId(), AppointmentStatus.BILLING);
    }

    private void seedInventory(Long centerId, String medicineName, BigDecimal sellingPrice) {
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
        batchRequest.setUnitPrice(new BigDecimal("6.00"));
        batchRequest.setSellingPrice(sellingPrice);
        inventoryService.addBatch(batchRequest);
    }

    private User createUser(RoleName roleName, String suffix) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName));

        User user = new User();
        user.setUsername(suffix);
        user.setPassword("encoded-password");
        user.setEmail(suffix + "@example.com");
        user.setPhone("9999999999");
        user.setRole(role);
        return userRepository.save(user);
    }

    private FamilyResponse createFamily(Long centerId, String headName) {
        CreateFamilyRequest familyRequest = new CreateFamilyRequest();
        familyRequest.setFamilyHeadName(headName);
        familyRequest.setCenterId(centerId);
        return familyService.registerFamily(familyRequest);
    }

    private FamilyMemberResponse createFamilyMember(String healthCardNumber) {
        CreateFamilyMemberRequest memberRequest = new CreateFamilyMemberRequest();
        memberRequest.setFirstName("Test");
        memberRequest.setLastName("Patient");
        memberRequest.setDob(LocalDate.of(1993, 3, 12));
        memberRequest.setGender("MALE");
        memberRequest.setBloodGroup("O+");
        return familyService.addFamilyMember(healthCardNumber, memberRequest);
    }

    private CenterResponse createCenterHierarchy(String codeSuffix) {
        CreateStateRequest stateRequest = new CreateStateRequest();
        stateRequest.setName("Bihar " + codeSuffix);
        stateRequest.setCode(codeSuffix);
        var state = stateService.create(stateRequest);

        CreateDistrictRequest districtRequest = new CreateDistrictRequest();
        districtRequest.setName("Patna " + codeSuffix);
        districtRequest.setStateId(state.getId());
        DistrictResponse district = districtService.create(districtRequest);

        CreateBlockRequest blockRequest = new CreateBlockRequest();
        blockRequest.setName("Bihta " + codeSuffix);
        blockRequest.setDistrictId(district.getId());
        BlockResponse block = blockService.create(blockRequest);

        CreateCenterRequest centerRequest = new CreateCenterRequest();
        centerRequest.setName("Billing Center " + codeSuffix);
        centerRequest.setCenterCode("BR-" + codeSuffix);
        centerRequest.setAddress("Main Road");
        centerRequest.setContactNumber("9876543210");
        centerRequest.setBlockId(block.getId());

        return centerService.create(centerRequest);
    }
}
