package com.apanaswastha.erp.dto;

import com.apanaswastha.erp.entity.enums.PaymentMethod;
import com.apanaswastha.erp.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class InvoiceResponse {

    private final Long id;
    private final Long appointmentId;
    private final Long familyId;
    private final BigDecimal totalAmount;
    private final PaymentStatus paymentStatus;
    private final PaymentMethod paymentMethod;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final List<InvoiceItemResponse> items;

    public InvoiceResponse(
            Long id,
            Long appointmentId,
            Long familyId,
            BigDecimal totalAmount,
            PaymentStatus paymentStatus,
            PaymentMethod paymentMethod,
            Instant createdAt,
            Instant updatedAt,
            List<InvoiceItemResponse> items
    ) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.familyId = familyId;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.items = items;
    }

    public Long getId() {
        return id;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public Long getFamilyId() {
        return familyId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<InvoiceItemResponse> getItems() {
        return items;
    }
}
