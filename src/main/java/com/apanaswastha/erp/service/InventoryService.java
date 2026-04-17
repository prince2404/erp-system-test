package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.CenterStockResponse;
import com.apanaswastha.erp.dto.CreateInventoryBatchRequest;
import com.apanaswastha.erp.dto.CreateMedicineRequest;
import com.apanaswastha.erp.dto.InventoryBatchResponse;
import com.apanaswastha.erp.dto.MedicineResponse;
import com.apanaswastha.erp.entity.Center;
import com.apanaswastha.erp.entity.InventoryBatch;
import com.apanaswastha.erp.entity.Medicine;
import com.apanaswastha.erp.entity.Vendor;
import com.apanaswastha.erp.exception.InsufficientStockException;
import com.apanaswastha.erp.exception.NotFoundException;
import com.apanaswastha.erp.repository.CenterRepository;
import com.apanaswastha.erp.repository.InventoryBatchRepository;
import com.apanaswastha.erp.repository.MedicineRepository;
import com.apanaswastha.erp.repository.VendorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final MedicineRepository medicineRepository;
    private final VendorRepository vendorRepository;
    private final CenterRepository centerRepository;
    private final InventoryBatchRepository inventoryBatchRepository;

    public InventoryService(
            MedicineRepository medicineRepository,
            VendorRepository vendorRepository,
            CenterRepository centerRepository,
            InventoryBatchRepository inventoryBatchRepository
    ) {
        this.medicineRepository = medicineRepository;
        this.vendorRepository = vendorRepository;
        this.centerRepository = centerRepository;
        this.inventoryBatchRepository = inventoryBatchRepository;
    }

    @Transactional
    public MedicineResponse addMedicine(CreateMedicineRequest request) {
        Medicine medicine = new Medicine();
        medicine.setName(request.getName());
        medicine.setGenericName(request.getGenericName());
        medicine.setManufacturer(request.getManufacturer());
        return toMedicineResponse(medicineRepository.save(medicine));
    }

    @Transactional
    public InventoryBatchResponse addBatch(CreateInventoryBatchRequest request) {
        Medicine medicine = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> new NotFoundException("Medicine not found with id: " + request.getMedicineId()));

        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new NotFoundException("Vendor not found with id: " + request.getVendorId()));

        Center center = centerRepository.findById(request.getCenterId())
                .orElseThrow(() -> new NotFoundException("Center not found with id: " + request.getCenterId()));

        InventoryBatch batch = new InventoryBatch();
        batch.setMedicine(medicine);
        batch.setVendor(vendor);
        batch.setCenter(center);
        batch.setBatchNumber(request.getBatchNumber());
        batch.setExpiryDate(request.getExpiryDate());
        batch.setQuantityReceived(request.getQuantityReceived());
        batch.setQuantityAvailable(request.getQuantityReceived());
        batch.setUnitPrice(request.getUnitPrice());
        batch.setSellingPrice(request.getSellingPrice());

        return toBatchResponse(inventoryBatchRepository.save(batch));
    }

    public List<CenterStockResponse> getCenterStock(Long centerId) {
        centerRepository.findById(centerId)
                .orElseThrow(() -> new NotFoundException("Center not found with id: " + centerId));

        List<InventoryBatch> batches = inventoryBatchRepository.findByCenterIdAndQuantityAvailableGreaterThan(centerId, 0);

        Map<Long, Integer> stockByMedicine = batches
                .stream()
                .collect(Collectors.groupingBy(
                        batch -> batch.getMedicine().getId(),
                        Collectors.summingInt(InventoryBatch::getQuantityAvailable)
                ));

        return batches.stream()
                .collect(Collectors.toMap(
                        batch -> batch.getMedicine().getId(),
                        batch -> batch.getMedicine().getName(),
                        (existing, ignored) -> existing
                ))
                .entrySet().stream()
                .map(entry -> new CenterStockResponse(entry.getKey(), entry.getValue(), stockByMedicine.get(entry.getKey())))
                .sorted(Comparator.comparing(CenterStockResponse::getMedicineName))
                .toList();
    }

    public List<InventoryBatchResponse> getExpiringBatches(Long centerId, int days) {
        centerRepository.findById(centerId)
                .orElseThrow(() -> new NotFoundException("Center not found with id: " + centerId));

        if (days != 30 && days != 60) {
            throw new IllegalArgumentException("days must be either 30 or 60, but received: " + days);
        }

        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(days);

        return inventoryBatchRepository
                .findByCenterIdAndExpiryDateBetweenAndQuantityAvailableGreaterThanOrderByExpiryDateAsc(centerId, today, limit, 0)
                .stream()
                .map(this::toBatchResponse)
                .toList();
    }

    @Transactional
    public void dispenseMedicine(Long centerId, String medicineName, int requiredQuantity) {
        if (requiredQuantity <= 0) {
            throw new IllegalArgumentException("Dispense quantity must be greater than zero");
        }

        centerRepository.findById(centerId)
                .orElseThrow(() -> new NotFoundException("Center not found with id: " + centerId));

        Medicine medicine = medicineRepository.findByNameIgnoreCase(medicineName)
                .orElseThrow(() -> new NotFoundException("Medicine not found with name: " + medicineName));

        List<InventoryBatch> batches = inventoryBatchRepository
                .findByCenterIdAndMedicineIdAndQuantityAvailableGreaterThanAndExpiryDateGreaterThanEqualOrderByExpiryDateAscCreatedAtAscIdAsc(
                        centerId,
                        medicine.getId(),
                        0,
                        LocalDate.now()
                );

        int totalAvailable = batches.stream().mapToInt(InventoryBatch::getQuantityAvailable).sum();
        if (totalAvailable < requiredQuantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for medicine " + medicineName + " at center " + centerId
            );
        }

        int remaining = requiredQuantity;
        for (InventoryBatch batch : batches) {
            if (remaining == 0) {
                break;
            }
            int deduction = Math.min(batch.getQuantityAvailable(), remaining);
            batch.setQuantityAvailable(batch.getQuantityAvailable() - deduction);
            remaining -= deduction;
        }
    }

    private MedicineResponse toMedicineResponse(Medicine medicine) {
        return new MedicineResponse(
                medicine.getId(),
                medicine.getName(),
                medicine.getGenericName(),
                medicine.getManufacturer(),
                medicine.getCreatedAt(),
                medicine.getUpdatedAt()
        );
    }

    private InventoryBatchResponse toBatchResponse(InventoryBatch batch) {
        return new InventoryBatchResponse(
                batch.getId(),
                batch.getMedicine().getId(),
                batch.getMedicine().getName(),
                batch.getVendor().getId(),
                batch.getCenter().getId(),
                batch.getBatchNumber(),
                batch.getExpiryDate(),
                batch.getQuantityReceived(),
                batch.getQuantityAvailable(),
                batch.getUnitPrice(),
                batch.getSellingPrice(),
                batch.getCreatedAt()
        );
    }
}
