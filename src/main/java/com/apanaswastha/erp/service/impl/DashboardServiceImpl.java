package com.apanaswastha.erp.service.impl;

import com.apanaswastha.erp.dto.response.dashboard.DashboardMetricsDTO;
import com.apanaswastha.erp.entity.User;
import com.apanaswastha.erp.enums.RoleName;
import com.apanaswastha.erp.repository.AppointmentRepository;
import com.apanaswastha.erp.repository.CenterRepository;
import com.apanaswastha.erp.repository.CommissionLedgerRepository;
import com.apanaswastha.erp.repository.InvoiceRepository;
import com.apanaswastha.erp.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import com.apanaswastha.erp.service.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final InvoiceRepository invoiceRepository;
    private final CenterRepository centerRepository;
    private final CommissionLedgerRepository commissionLedgerRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    public DashboardServiceImpl(
            InvoiceRepository invoiceRepository,
            CenterRepository centerRepository,
            CommissionLedgerRepository commissionLedgerRepository,
            AppointmentRepository appointmentRepository,
            UserRepository userRepository
    ) {
        this.invoiceRepository = invoiceRepository;
        this.centerRepository = centerRepository;
        this.commissionLedgerRepository = commissionLedgerRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    public DashboardMetricsDTO getMetricsForUser(String username) {
        User user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Scope scope = resolveScope(user);
        if (scope.denyAllScope()) {
            return new DashboardMetricsDTO(BigDecimal.ZERO, 0, BigDecimal.ZERO, 0);
        }

        BigDecimal revenue = nonNull(invoiceRepository.sumPaidRevenueByScope(
                scope.stateId(),
                scope.districtId(),
                scope.blockId(),
                scope.centerId()
        ));

        long activeCenters = centerRepository.countByScope(
                scope.stateId(),
                scope.districtId(),
                scope.blockId(),
                scope.centerId()
        );

        BigDecimal pendingCommissions = nonNull(commissionLedgerRepository.sumPendingAmountByScope(
                scope.stateId(),
                scope.districtId(),
                scope.blockId(),
                scope.centerId()
        ));

        long dailyVisits = appointmentRepository.countByAppointmentDateAndScope(
                LocalDate.now(),
                scope.stateId(),
                scope.districtId(),
                scope.blockId(),
                scope.centerId()
        );

        return new DashboardMetricsDTO(revenue, activeCenters, pendingCommissions, dailyVisits);
    }

    private Scope resolveScope(User user) {
        RoleName roleName = user.getRole().getName();
        if (roleName == RoleName.SUPER_ADMIN || roleName == RoleName.ADMIN) {
            return Scope.unscoped();
        }

        if (user.getAssignedCenter() != null) {
            return Scope.center(user.getAssignedCenter().getId());
        }

        if (roleName == RoleName.BLOCK_MANAGER) {
            return user.getAssignedBlock() != null
                    ? Scope.block(user.getAssignedBlock().getId())
                    : Scope.restricted();
        }

        if (roleName == RoleName.DISTRICT_MANAGER) {
            return user.getAssignedDistrict() != null
                    ? Scope.district(user.getAssignedDistrict().getId())
                    : Scope.restricted();
        }

        if (roleName == RoleName.STATE_MANAGER) {
            return user.getAssignedState() != null
                    ? Scope.state(user.getAssignedState().getId())
                    : Scope.restricted();
        }

        if (user.getAssignedBlock() != null) {
            return Scope.block(user.getAssignedBlock().getId());
        }
        if (user.getAssignedDistrict() != null) {
            return Scope.district(user.getAssignedDistrict().getId());
        }
        if (user.getAssignedState() != null) {
            return Scope.state(user.getAssignedState().getId());
        }
        return Scope.restricted();
    }

    private BigDecimal nonNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private record Scope(Long stateId, Long districtId, Long blockId, Long centerId, boolean denyAllScope) {
        private static Scope unscoped() {
            return new Scope(null, null, null, null, false);
        }

        private static Scope state(Long stateId) {
            return new Scope(stateId, null, null, null, false);
        }

        private static Scope district(Long districtId) {
            return new Scope(null, districtId, null, null, false);
        }

        private static Scope block(Long blockId) {
            return new Scope(null, null, blockId, null, false);
        }

        private static Scope center(Long centerId) {
            return new Scope(null, null, null, centerId, false);
        }

        private static Scope restricted() {
            return new Scope(null, null, null, null, true);
        }
    }
}
