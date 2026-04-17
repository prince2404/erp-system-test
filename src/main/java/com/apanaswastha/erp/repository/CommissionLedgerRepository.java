package com.apanaswastha.erp.repository;

import com.apanaswastha.erp.entity.CommissionLedger;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface CommissionLedgerRepository extends JpaRepository<CommissionLedger, Long> {

    List<CommissionLedger> findByRecipientUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByInvoiceId(Long invoiceId);

    @Query("""
            select coalesce(sum(cl.amount), 0)
            from CommissionLedger cl
            where cl.status = com.apanaswastha.erp.entity.enums.CommissionStatus.PENDING
              and (:stateId is null or cl.invoice.appointment.center.block.district.state.id = :stateId)
              and (:districtId is null or cl.invoice.appointment.center.block.district.id = :districtId)
              and (:blockId is null or cl.invoice.appointment.center.block.id = :blockId)
              and (:centerId is null or cl.invoice.appointment.center.id = :centerId)
            """)
    BigDecimal sumPendingAmountByScope(
            @Param("stateId") Long stateId,
            @Param("districtId") Long districtId,
            @Param("blockId") Long blockId,
            @Param("centerId") Long centerId
    );
}
