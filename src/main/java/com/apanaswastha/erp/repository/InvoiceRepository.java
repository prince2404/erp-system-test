package com.apanaswastha.erp.repository;

import com.apanaswastha.erp.entity.Invoice;
import com.apanaswastha.erp.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByAppointmentId(Long appointmentId);

    List<Invoice> findByPaymentStatusOrderByCreatedAtDesc(PaymentStatus paymentStatus);

    @Query("""
            select coalesce(sum(i.totalAmount), 0)
            from Invoice i
            where i.paymentStatus = com.apanaswastha.erp.entity.enums.PaymentStatus.PAID
              and (:stateId is null or i.appointment.center.block.district.state.id = :stateId)
              and (:districtId is null or i.appointment.center.block.district.id = :districtId)
              and (:blockId is null or i.appointment.center.block.id = :blockId)
              and (:centerId is null or i.appointment.center.id = :centerId)
            """)
    BigDecimal sumPaidRevenueByScope(
            @Param("stateId") Long stateId,
            @Param("districtId") Long districtId,
            @Param("blockId") Long blockId,
            @Param("centerId") Long centerId
    );
}
