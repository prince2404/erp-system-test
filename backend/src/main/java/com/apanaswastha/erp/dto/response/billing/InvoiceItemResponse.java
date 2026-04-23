package com.apanaswastha.erp.dto.response.billing;

import com.apanaswastha.erp.enums.InvoiceItemType;

import java.math.BigDecimal;

public class InvoiceItemResponse {

    private final Long id;
    private final InvoiceItemType itemType;
    private final Long referenceId;
    private final Integer quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal subtotal;

    public InvoiceItemResponse(
            Long id,
            InvoiceItemType itemType,
            Long referenceId,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
        this.id = id;
        this.itemType = itemType;
        this.referenceId = referenceId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public Long getId() {
        return id;
    }

    public InvoiceItemType getItemType() {
        return itemType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
