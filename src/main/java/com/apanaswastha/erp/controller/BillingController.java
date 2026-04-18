package com.apanaswastha.erp.controller;

import com.apanaswastha.erp.dto.InvoiceResponse;
import com.apanaswastha.erp.dto.PayInvoiceRequest;
import com.apanaswastha.erp.entity.enums.PaymentStatus;
import com.apanaswastha.erp.payload.ApiResponse;
import com.apanaswastha.erp.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Invoices", description = "Invoice generation, retrieval, and payment APIs")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping("/generate/{appointmentId}")
    @Operation(summary = "Generate invoice", description = "Generates an invoice for the provided appointment ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invoice generated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    public ApiResponse<InvoiceResponse> generateInvoice(@PathVariable Long appointmentId) {
        return ApiResponse.success("Invoice generated successfully", billingService.generateInvoice(appointmentId));
    }

    @PostMapping("/{id}/pay")
    @Operation(summary = "Pay invoice", description = "Processes invoice payment using the requested payment method")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invoice paid successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Payment could not be processed")
    })
    public ApiResponse<InvoiceResponse> payInvoice(@PathVariable Long id, @Valid @RequestBody PayInvoiceRequest request) {
        return ApiResponse.success("Invoice paid successfully", billingService.processPayment(id, request.getPaymentMethod()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get invoice", description = "Returns invoice details by invoice ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invoice fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    public ApiResponse<InvoiceResponse> getInvoice(@PathVariable Long id) {
        return ApiResponse.success("Invoice fetched successfully", billingService.getInvoice(id));
    }

    @GetMapping
    @Operation(summary = "List invoices", description = "Lists invoices with optional payment status filter")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invoices fetched successfully")
    })
    public ApiResponse<List<InvoiceResponse>> listInvoices(@RequestParam(required = false) PaymentStatus status) {
        return ApiResponse.success("Invoices fetched successfully", billingService.listInvoices(status));
    }
}
