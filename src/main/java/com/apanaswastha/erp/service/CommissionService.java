package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.CommissionLedgerResponse;
import com.apanaswastha.erp.entity.CommissionLedger;
import com.apanaswastha.erp.entity.Invoice;
import com.apanaswastha.erp.entity.Role;
import com.apanaswastha.erp.entity.User;
import com.apanaswastha.erp.entity.enums.CommissionStatus;
import com.apanaswastha.erp.entity.enums.RoleName;
import com.apanaswastha.erp.repository.CommissionLedgerRepository;
import com.apanaswastha.erp.repository.RoleRepository;
import com.apanaswastha.erp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommissionService {

    private static final Map<RoleName, BigDecimal> COMMISSION_DISTRIBUTION = new LinkedHashMap<>();

    static {
        COMMISSION_DISTRIBUTION.put(RoleName.ASSOCIATE, new BigDecimal("4.00"));
        COMMISSION_DISTRIBUTION.put(RoleName.BLOCK_MANAGER, new BigDecimal("3.00"));
        COMMISSION_DISTRIBUTION.put(RoleName.DISTRICT_MANAGER, new BigDecimal("2.00"));
        COMMISSION_DISTRIBUTION.put(RoleName.STATE_MANAGER, new BigDecimal("1.00"));
        COMMISSION_DISTRIBUTION.put(RoleName.SUPER_ADMIN, new BigDecimal("0.50"));
    }

    private final CommissionLedgerRepository commissionLedgerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public CommissionService(
            CommissionLedgerRepository commissionLedgerRepository,
            UserRepository userRepository,
            RoleRepository roleRepository
    ) {
        this.commissionLedgerRepository = commissionLedgerRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public void distributeForPaidInvoice(Invoice invoice) {
        if (commissionLedgerRepository.existsByInvoiceId(invoice.getId())) {
            return;
        }

        for (Map.Entry<RoleName, BigDecimal> entry : COMMISSION_DISTRIBUTION.entrySet()) {
            RoleName roleName = entry.getKey();
            BigDecimal percentage = entry.getValue();

            Role role = roleRepository.findByName(roleName).orElse(null);
            if (role == null) {
                continue;
            }

            User recipient = resolveRecipient(roleName);
            if (recipient == null) {
                continue;
            }

            CommissionLedger ledger = new CommissionLedger();
            ledger.setInvoice(invoice);
            ledger.setRecipientUser(recipient);
            ledger.setRole(role);
            ledger.setPercentageApplied(percentage);
            ledger.setAmount(invoice.getTotalAmount()
                    .multiply(percentage)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
            ledger.setStatus(CommissionStatus.PENDING);
            commissionLedgerRepository.save(ledger);
        }
    }

    public List<CommissionLedgerResponse> getUserCommissions(Long userId) {
        return commissionLedgerRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private User resolveRecipient(RoleName roleName) {
        if (roleName == RoleName.SUPER_ADMIN) {
            return userRepository.findFirstByRoleNameAndIsDeletedFalseOrderByIdAsc(RoleName.SUPER_ADMIN)
                    .or(() -> userRepository.findFirstByRoleNameAndIsDeletedFalseOrderByIdAsc(RoleName.ADMIN))
                    .orElse(null);
        }

        return userRepository.findFirstByRoleNameAndIsDeletedFalseOrderByIdAsc(roleName)
                .orElse(null);
    }

    private CommissionLedgerResponse toResponse(CommissionLedger ledger) {
        return new CommissionLedgerResponse(
                ledger.getId(),
                ledger.getInvoice().getId(),
                ledger.getRecipientUser() != null ? ledger.getRecipientUser().getId() : null,
                ledger.getRole().getId(),
                ledger.getAmount(),
                ledger.getPercentageApplied(),
                ledger.getStatus(),
                ledger.getCreatedAt()
        );
    }
}
