package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.InvoiceItemResponse;
import com.apanaswastha.erp.dto.InvoiceResponse;
import com.apanaswastha.erp.entity.Appointment;
import com.apanaswastha.erp.entity.Family;
import com.apanaswastha.erp.entity.InventoryBatch;
import com.apanaswastha.erp.entity.Invoice;
import com.apanaswastha.erp.entity.InvoiceItem;
import com.apanaswastha.erp.entity.Medicine;
import com.apanaswastha.erp.entity.Prescription;
import com.apanaswastha.erp.entity.enums.AppointmentStatus;
import com.apanaswastha.erp.entity.enums.InvoiceItemType;
import com.apanaswastha.erp.entity.enums.PaymentMethod;
import com.apanaswastha.erp.entity.enums.PaymentStatus;
import com.apanaswastha.erp.exception.InsufficientBalanceException;
import com.apanaswastha.erp.exception.NotFoundException;
import com.apanaswastha.erp.repository.AppointmentRepository;
import com.apanaswastha.erp.repository.FamilyRepository;
import com.apanaswastha.erp.repository.InventoryBatchRepository;
import com.apanaswastha.erp.repository.InvoiceRepository;
import com.apanaswastha.erp.repository.MedicineRepository;
import com.apanaswastha.erp.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BillingService {

    private static final BigDecimal CONSULTATION_FEE = new BigDecimal("100.00");

    private final InvoiceRepository invoiceRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicineRepository medicineRepository;
    private final InventoryBatchRepository inventoryBatchRepository;
    private final FamilyRepository familyRepository;
    private final CommissionService commissionService;

    public BillingService(
            InvoiceRepository invoiceRepository,
            AppointmentRepository appointmentRepository,
            PrescriptionRepository prescriptionRepository,
            MedicineRepository medicineRepository,
            InventoryBatchRepository inventoryBatchRepository,
            FamilyRepository familyRepository,
            CommissionService commissionService
    ) {
        this.invoiceRepository = invoiceRepository;
        this.appointmentRepository = appointmentRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.medicineRepository = medicineRepository;
        this.inventoryBatchRepository = inventoryBatchRepository;
        this.familyRepository = familyRepository;
        this.commissionService = commissionService;
    }

    @Transactional
    public InvoiceResponse generateInvoice(Long appointmentId) {
        Invoice existing = invoiceRepository.findByAppointmentId(appointmentId).orElse(null);
        if (existing != null) {
            return toResponse(existing);
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found with id: " + appointmentId));

        if (appointment.getStatus() != AppointmentStatus.BILLING) {
            throw new IllegalArgumentException(
                    "Invoice can only be generated when appointment is in BILLING status. Current status: " + appointment.getStatus()
            );
        }

        Family family = appointment.getPatient().getFamily();

        Invoice invoice = new Invoice();
        invoice.setAppointment(appointment);
        invoice.setFamily(family);
        invoice.setPaymentStatus(PaymentStatus.PENDING);

        List<InvoiceItem> items = new ArrayList<>();
        items.add(createConsultationItem(invoice, appointment));

        List<Prescription> dispensedPrescriptions = prescriptionRepository
                .findByDiagnosisAppointmentIdAndDispensedTrue(appointmentId);

        Map<String, Long> quantityByMedicineName = dispensedPrescriptions.stream()
                .map(Prescription::getMedicineName)
                .map(this::normalizeMedicineName)
                .collect(Collectors.groupingBy(
                        normalizedName -> normalizedName,
                        Collectors.counting()
                ));

        for (Map.Entry<String, Long> entry : quantityByMedicineName.entrySet()) {
            String medicineName = entry.getKey();
            int quantity = entry.getValue().intValue();

            Medicine medicine = medicineRepository.findByNameIgnoreCase(medicineName)
                    .orElseThrow(() -> new NotFoundException("Medicine not found with name: " + medicineName));

            InventoryBatch pricingBatch = inventoryBatchRepository
                    .findTopByCenterIdAndMedicineIdOrderByExpiryDateAscCreatedAtAscIdAsc(
                            appointment.getCenter().getId(),
                            medicine.getId()
                    )
                    .orElseThrow(() -> new NotFoundException(
                            "Inventory pricing batch not found for medicine id: " + medicine.getId()
                    ));

            BigDecimal unitPrice = pricingBatch.getSellingPrice().setScale(2, RoundingMode.HALF_UP);
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);

            InvoiceItem medicineItem = new InvoiceItem();
            medicineItem.setInvoice(invoice);
            medicineItem.setItemType(InvoiceItemType.MEDICINE);
            medicineItem.setReferenceId(medicine.getId());
            medicineItem.setQuantity(quantity);
            medicineItem.setUnitPrice(unitPrice);
            medicineItem.setSubtotal(subtotal);
            items.add(medicineItem);
        }

        invoice.setItems(items);
        invoice.setTotalAmount(items.stream()
                .map(InvoiceItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP));

        return toResponse(invoiceRepository.save(invoice));
    }

    @Transactional
    public InvoiceResponse processPayment(Long invoiceId, PaymentMethod paymentMethod) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found with id: " + invoiceId));

        if (invoice.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException("Paid invoices are immutable and cannot be modified");
        }

        if (invoice.getPaymentStatus() == PaymentStatus.REFUNDED) {
            throw new IllegalArgumentException("Refunded invoice cannot be paid again");
        }

        if (paymentMethod == PaymentMethod.WALLET) {
            Family family = invoice.getFamily();
            BigDecimal balance = family.getWalletBalance();
            if (balance.compareTo(invoice.getTotalAmount()) < 0) {
                throw new InsufficientBalanceException(
                        "Insufficient wallet balance: required " + invoice.getTotalAmount() + ", available " + balance
                );
            }

            family.setWalletBalance(balance.subtract(invoice.getTotalAmount()).setScale(2, RoundingMode.HALF_UP));
            familyRepository.save(family);
        }

        invoice.setPaymentMethod(paymentMethod);
        invoice.setPaymentStatus(PaymentStatus.PAID);
        invoice = invoiceRepository.save(invoice);

        commissionService.distributeForPaidInvoice(invoice);

        Appointment appointment = invoice.getAppointment();
        if (appointment.getStatus() == AppointmentStatus.BILLING) {
            appointment.setStatus(AppointmentStatus.COMPLETED);
            appointmentRepository.save(appointment);
        }

        return toResponse(invoice);
    }

    public InvoiceResponse getInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Invoice not found with id: " + id));
        return toResponse(invoice);
    }

    public List<InvoiceResponse> listInvoices(PaymentStatus status) {
        List<Invoice> invoices = status == null
                ? invoiceRepository.findAll()
                : invoiceRepository.findByPaymentStatusOrderByCreatedAtDesc(status);

        return invoices.stream()
                .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
                .map(this::toResponse)
                .toList();
    }

    private InvoiceItem createConsultationItem(Invoice invoice, Appointment appointment) {
        InvoiceItem item = new InvoiceItem();
        item.setInvoice(invoice);
        item.setItemType(InvoiceItemType.CONSULTATION);
        item.setReferenceId(appointment.getDoctor().getId());
        item.setQuantity(1);
        item.setUnitPrice(CONSULTATION_FEE);
        item.setSubtotal(CONSULTATION_FEE);
        return item;
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        List<InvoiceItemResponse> itemResponses = invoice.getItems()
                .stream()
                .map(item -> new InvoiceItemResponse(
                        item.getId(),
                        item.getItemType(),
                        item.getReferenceId(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ))
                .toList();

        return new InvoiceResponse(
                invoice.getId(),
                invoice.getAppointment().getId(),
                invoice.getFamily().getId(),
                invoice.getTotalAmount(),
                invoice.getPaymentStatus(),
                invoice.getPaymentMethod(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt(),
                itemResponses
        );
    }

    private String normalizeMedicineName(String medicineName) {
        return medicineName == null ? "" : medicineName.trim().toLowerCase();
    }
}
