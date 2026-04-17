package com.apanaswastha.erp.controller;

import com.apanaswastha.erp.dto.CenterStockResponse;
import com.apanaswastha.erp.dto.CreateInventoryBatchRequest;
import com.apanaswastha.erp.dto.CreateMedicineRequest;
import com.apanaswastha.erp.dto.InventoryBatchResponse;
import com.apanaswastha.erp.dto.MedicineResponse;
import com.apanaswastha.erp.payload.ApiResponse;
import com.apanaswastha.erp.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/medicines")
    public ResponseEntity<ApiResponse<MedicineResponse>> addMedicine(@Valid @RequestBody CreateMedicineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Medicine added successfully", inventoryService.addMedicine(request)));
    }

    @PostMapping("/batches")
    public ResponseEntity<ApiResponse<InventoryBatchResponse>> addBatch(@Valid @RequestBody CreateInventoryBatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inventory batch added successfully", inventoryService.addBatch(request)));
    }

    @GetMapping("/centers/{centerId}/stock")
    public ApiResponse<List<CenterStockResponse>> getCenterStock(@PathVariable Long centerId) {
        return ApiResponse.success("Current stock fetched successfully", inventoryService.getCenterStock(centerId));
    }

    @GetMapping("/centers/{centerId}/expiring")
    public ApiResponse<List<InventoryBatchResponse>> getExpiringBatches(
            @PathVariable Long centerId,
            @RequestParam(defaultValue = "30") int days
    ) {
        return ApiResponse.success("Expiring batches fetched successfully", inventoryService.getExpiringBatches(centerId, days));
    }
}
