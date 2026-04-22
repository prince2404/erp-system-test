package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.request.inventory.CreateInventoryBatchRequest;
import com.apanaswastha.erp.dto.request.inventory.CreateMedicineRequest;
import com.apanaswastha.erp.dto.response.inventory.CenterStockResponse;
import com.apanaswastha.erp.dto.response.inventory.InventoryBatchResponse;
import com.apanaswastha.erp.dto.response.inventory.MedicineResponse;

import java.util.List;

public interface InventoryService {

    /**
     * Adds a medicine catalog item.
     *
     * @param request medicine payload
     * @return medicine details
     */
    MedicineResponse addMedicine(CreateMedicineRequest request);

    /**
     * Lists medicines.
     *
     * @return medicine list
     */
    List<MedicineResponse> getMedicines();

    /**
     * Adds inventory batch for center stock.
     *
     * @param request batch payload
     * @return created batch
     */
    InventoryBatchResponse addBatch(CreateInventoryBatchRequest request);

    /**
     * Returns stock summary for center.
     *
     * @param centerId center id
     * @return stock entries
     */
    List<CenterStockResponse> getCenterStock(Long centerId);

    /**
     * Lists inventory batches for center.
     *
     * @param centerId center id
     * @return batch list
     */
    List<InventoryBatchResponse> getCenterBatches(Long centerId);

    /**
     * Lists expiring batches within N days.
     *
     * @param centerId center id
     * @param days number of days
     * @return expiring batch list
     */
    List<InventoryBatchResponse> getExpiringBatches(Long centerId, int days);

    /**
     * Dispenses medicine quantity from inventory.
     *
     * @param centerId center id
     * @param medicineName medicine name
     * @param requiredQuantity quantity to dispense
     */
    void dispenseMedicine(Long centerId, String medicineName, int requiredQuantity);
}
