package com.apanaswastha.erp.dto.request.billing;

import com.apanaswastha.erp.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public class PayInvoiceRequest {

    @NotNull
    private PaymentMethod paymentMethod;

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
