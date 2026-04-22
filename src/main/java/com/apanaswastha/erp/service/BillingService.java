package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.response.billing.InvoiceResponse;
import com.apanaswastha.erp.enums.PaymentMethod;
import com.apanaswastha.erp.enums.PaymentStatus;

import java.util.List;

public interface BillingService {

    /**
     * Generates an invoice for an appointment.
     *
     * @param appointmentId appointment identifier
     * @return generated invoice details
     */
    InvoiceResponse generateInvoice(Long appointmentId);

    /**
     * Processes invoice payment.
     *
     * @param invoiceId invoice identifier
     * @param paymentMethod selected payment method
     * @return updated invoice details
     */
    InvoiceResponse processPayment(Long invoiceId, PaymentMethod paymentMethod);

    /**
     * Fetches a single invoice.
     *
     * @param id invoice identifier
     * @return invoice details
     */
    InvoiceResponse getInvoice(Long id);

    /**
     * Lists invoices by optional payment status.
     *
     * @param status payment status filter
     * @return list of invoices
     */
    List<InvoiceResponse> listInvoices(PaymentStatus status);
}
