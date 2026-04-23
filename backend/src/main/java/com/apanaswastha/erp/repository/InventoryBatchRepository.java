package com.apanaswastha.erp.repository;

import com.apanaswastha.erp.entity.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long> {

    List<InventoryBatch> findByCenterIdAndQuantityAvailableGreaterThan(Long centerId, Integer quantity);

    List<InventoryBatch> findByCenterIdAndMedicineIdAndQuantityAvailableGreaterThanAndExpiryDateGreaterThanEqualOrderByExpiryDateAscCreatedAtAscIdAsc(
            Long centerId,
            Long medicineId,
            Integer quantity,
            LocalDate date
    );

    List<InventoryBatch> findByCenterIdAndExpiryDateBetweenAndQuantityAvailableGreaterThanOrderByExpiryDateAsc(
            Long centerId,
            LocalDate startDate,
            LocalDate endDate,
            Integer quantity
    );

    Optional<InventoryBatch> findTopByCenterIdAndMedicineIdOrderByExpiryDateAscCreatedAtAscIdAsc(Long centerId, Long medicineId);
}
