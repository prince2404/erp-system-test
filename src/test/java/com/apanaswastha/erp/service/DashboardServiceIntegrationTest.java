package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.DashboardMetricsDTO;
import com.apanaswastha.erp.entity.Appointment;
import com.apanaswastha.erp.entity.Block;
import com.apanaswastha.erp.entity.Center;
import com.apanaswastha.erp.entity.CommissionLedger;
import com.apanaswastha.erp.entity.District;
import com.apanaswastha.erp.entity.Family;
import com.apanaswastha.erp.entity.FamilyMember;
import com.apanaswastha.erp.entity.Invoice;
import com.apanaswastha.erp.entity.Role;
import com.apanaswastha.erp.entity.State;
import com.apanaswastha.erp.entity.User;
import com.apanaswastha.erp.entity.enums.AppointmentStatus;
import com.apanaswastha.erp.entity.enums.CommissionStatus;
import com.apanaswastha.erp.entity.enums.PaymentMethod;
import com.apanaswastha.erp.entity.enums.PaymentStatus;
import com.apanaswastha.erp.entity.enums.RoleName;
import com.apanaswastha.erp.repository.AppointmentRepository;
import com.apanaswastha.erp.repository.BlockRepository;
import com.apanaswastha.erp.repository.CenterRepository;
import com.apanaswastha.erp.repository.CommissionLedgerRepository;
import com.apanaswastha.erp.repository.DistrictRepository;
import com.apanaswastha.erp.repository.FamilyMemberRepository;
import com.apanaswastha.erp.repository.FamilyRepository;
import com.apanaswastha.erp.repository.InvoiceRepository;
import com.apanaswastha.erp.repository.RoleRepository;
import com.apanaswastha.erp.repository.StateRepository;
import com.apanaswastha.erp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DashboardServiceIntegrationTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private CenterRepository centerRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FamilyRepository familyRepository;

    @Autowired
    private FamilyMemberRepository familyMemberRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CommissionLedgerRepository commissionLedgerRepository;

    @Test
    void shouldReturnPlatformWideMetricsForSuperAdmin() {
        Setup setup = seedTwoStateDashboardData();

        User superAdmin = createUser(RoleName.SUPER_ADMIN, "super-metrics");

        DashboardMetricsDTO metrics = dashboardService.getMetricsForUser(superAdmin.getUsername());

        assertEquals(new BigDecimal("300.00"), metrics.getTotalRevenue());
        assertEquals(2L, metrics.getTotalActiveCenters());
        assertEquals(new BigDecimal("15.00"), metrics.getTotalPendingCommissions());
        assertEquals(2L, metrics.getDailyOpdVisits());
    }

    @Test
    void shouldScopeMetricsForStateAndDistrictManagerAssignments() {
        Setup setup = seedTwoStateDashboardData();

        User stateManager = createUser(RoleName.STATE_MANAGER, "state-metrics");
        stateManager.setAssignedState(setup.stateOne());
        userRepository.save(stateManager);

        User districtManager = createUser(RoleName.DISTRICT_MANAGER, "district-metrics");
        districtManager.setAssignedDistrict(setup.districtOne());
        userRepository.save(districtManager);

        DashboardMetricsDTO stateMetrics = dashboardService.getMetricsForUser(stateManager.getUsername());
        assertEquals(new BigDecimal("100.00"), stateMetrics.getTotalRevenue());
        assertEquals(1L, stateMetrics.getTotalActiveCenters());
        assertEquals(new BigDecimal("10.00"), stateMetrics.getTotalPendingCommissions());
        assertEquals(1L, stateMetrics.getDailyOpdVisits());

        DashboardMetricsDTO districtMetrics = dashboardService.getMetricsForUser(districtManager.getUsername());
        assertEquals(new BigDecimal("100.00"), districtMetrics.getTotalRevenue());
        assertEquals(1L, districtMetrics.getTotalActiveCenters());
        assertEquals(new BigDecimal("10.00"), districtMetrics.getTotalPendingCommissions());
        assertEquals(1L, districtMetrics.getDailyOpdVisits());
    }

    private Setup seedTwoStateDashboardData() {
        State stateOne = createState("Bihar", "BR");
        District districtOne = createDistrict(stateOne, "Patna");
        Block blockOne = createBlock(districtOne, "Bihta");
        Center centerOne = createCenter(blockOne, "Center One", "BR-PAT-001");

        State stateTwo = createState("Jharkhand", "JH");
        District districtTwo = createDistrict(stateTwo, "Ranchi");
        Block blockTwo = createBlock(districtTwo, "Kanke");
        Center centerTwo = createCenter(blockTwo, "Center Two", "JH-RAN-001");

        User doctor = createUser(RoleName.DOCTOR, "doctor-metrics");
        Role commissionRole = roleRepository.findByName(RoleName.STATE_MANAGER)
                .orElseThrow(() -> new IllegalStateException("Role not found: STATE_MANAGER"));

        Family familyOne = createFamily(centerOne, "Family One");
        Appointment todayCenterOne = createAppointment(centerOne, doctor, familyOne, "C1-TODAY", LocalDate.now());
        createPaidInvoice(todayCenterOne, familyOne, new BigDecimal("100.00"));

        Family familyTwo = createFamily(centerTwo, "Family Two");
        Appointment todayCenterTwo = createAppointment(centerTwo, doctor, familyTwo, "C2-TODAY", LocalDate.now());
        createPaidInvoice(todayCenterTwo, familyTwo, new BigDecimal("200.00"));

        Family familyOld = createFamily(centerOne, "Family Old");
        createAppointment(centerOne, doctor, familyOld, "C1-OLD", LocalDate.now().minusDays(1));

        createPendingCommission(todayCenterOne, commissionRole, new BigDecimal("10.00"));
        createPendingCommission(todayCenterTwo, commissionRole, new BigDecimal("5.00"));

        return new Setup(stateOne, districtOne);
    }

    private State createState(String name, String code) {
        State state = new State();
        state.setName(name);
        state.setCode(code);
        return stateRepository.save(state);
    }

    private District createDistrict(State state, String name) {
        District district = new District();
        district.setName(name);
        district.setState(state);
        return districtRepository.save(district);
    }

    private Block createBlock(District district, String name) {
        Block block = new Block();
        block.setName(name);
        block.setDistrict(district);
        return blockRepository.save(block);
    }

    private Center createCenter(Block block, String name, String centerCode) {
        Center center = new Center();
        center.setName(name);
        center.setCenterCode(centerCode);
        center.setAddress("Main Road");
        center.setContactNumber("9999999999");
        center.setBlock(block);
        return centerRepository.save(center);
    }

    private User createUser(RoleName roleName, String username) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName));

        User user = new User();
        user.setUsername(username);
        user.setPassword("encoded-password");
        user.setEmail(username + "@example.com");
        user.setPhone("9999999999");
        user.setRole(role);
        return userRepository.save(user);
    }

    private Family createFamily(Center center, String headName) {
        Family family = new Family();
        family.setFamilyHeadName(headName);
        family.setHealthCardNumber("CARD-" + headName.replace(" ", "-").toUpperCase());
        family.setQrCodeReference("QR-" + headName.replace(" ", "-").toUpperCase());
        family.setWalletBalance(new BigDecimal("0.00"));
        family.setCenter(center);
        Family savedFamily = familyRepository.save(family);

        FamilyMember member = new FamilyMember();
        member.setFirstName("Member");
        member.setLastName(headName.replace(" ", ""));
        member.setDob(LocalDate.of(1995, 1, 1));
        member.setGender("MALE");
        member.setBloodGroup("O+");
        member.setFamily(savedFamily);
        familyMemberRepository.save(member);
        return savedFamily;
    }

    private Appointment createAppointment(Center center, User doctor, Family family, String token, LocalDate date) {
        FamilyMember member = familyMemberRepository.findAll().stream()
                .filter(m -> m.getFamily().getId().equals(family.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Family member not found"));

        Appointment appointment = new Appointment();
        appointment.setPatient(member);
        appointment.setCenter(center);
        appointment.setDoctor(doctor);
        appointment.setTokenNumber(token);
        appointment.setStatus(AppointmentStatus.BILLING);
        appointment.setAppointmentDate(date);
        return appointmentRepository.save(appointment);
    }

    private Invoice createPaidInvoice(Appointment appointment, Family family, BigDecimal amount) {
        Invoice invoice = new Invoice();
        invoice.setAppointment(appointment);
        invoice.setFamily(family);
        invoice.setTotalAmount(amount);
        invoice.setPaymentStatus(PaymentStatus.PAID);
        invoice.setPaymentMethod(PaymentMethod.CASH);
        return invoiceRepository.save(invoice);
    }

    private void createPendingCommission(Appointment appointment, Role role, BigDecimal amount) {
        Invoice invoice = invoiceRepository.findByAppointmentId(appointment.getId())
                .orElseThrow(() -> new IllegalStateException("Invoice not found for appointment"));

        CommissionLedger ledger = new CommissionLedger();
        ledger.setInvoice(invoice);
        ledger.setRole(role);
        ledger.setAmount(amount);
        ledger.setPercentageApplied(BigDecimal.ONE);
        ledger.setStatus(CommissionStatus.PENDING);
        commissionLedgerRepository.save(ledger);
    }

    private record Setup(State stateOne, District districtOne) {
    }
}
