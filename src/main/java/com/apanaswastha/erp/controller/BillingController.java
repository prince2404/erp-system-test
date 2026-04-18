package com.apanaswastha.erp.controller;

import com.apanaswastha.erp.dto.InvoiceResponse;
import com.apanaswastha.erp.dto.PayInvoiceRequest;
import com.apanaswastha.erp.entity.enums.PaymentStatus;
import com.apanaswastha.erp.payload.ApiResponse;
import com.apanaswastha.erp.service.BillingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/billing/invoices")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping("/generate/{appointmentId}")
    public ApiResponse<InvoiceResponse> generateInvoice(@PathVariable Long appointmentId) {
        return ApiResponse.success("Invoice generated successfully", billingService.generateInvoice(appointmentId));
    }

    @PostMapping("/{id}/pay")
    public ApiResponse<InvoiceResponse> payInvoice(@PathVariable Long id, @Valid @RequestBody PayInvoiceRequest request) {
        return ApiResponse.success("Invoice paid successfully", billingService.processPayment(id, request.getPaymentMethod()));
    }

    @GetMapping("/{id}")
    public ApiResponse<InvoiceResponse> getInvoice(@PathVariable Long id) {
        return ApiResponse.success("Invoice fetched successfully", billingService.getInvoice(id));
    }

    @GetMapping
    public ApiResponse<List<InvoiceResponse>> listInvoices(@RequestParam(required = false) PaymentStatus status) {
        return ApiResponse.success("Invoices fetched successfully", billingService.listInvoices(status));
    }
}
