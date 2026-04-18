package com.apanaswastha.erp.controller;

import com.apanaswastha.erp.dto.CenterStockResponse;
import com.apanaswastha.erp.dto.CreateInventoryBatchRequest;
import com.apanaswastha.erp.dto.CreateMedicineRequest;
import com.apanaswastha.erp.dto.InventoryBatchResponse;
import com.apanaswastha.erp.dto.MedicineResponse;
import com.apanaswastha.erp.payload.ApiResponse;
import com.apanaswastha.erp.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Inventory", description = "Medicine catalog and inventory batch management APIs")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/medicines")
    @Operation(summary = "Add medicine", description = "Adds a medicine to the global medicine catalog")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Medicine added successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    public ResponseEntity<ApiResponse<MedicineResponse>> addMedicine(@Valid @RequestBody CreateMedicineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Medicine added successfully", inventoryService.addMedicine(request)));
    }

    @GetMapping("/medicines")
    @Operation(summary = "List medicines", description = "Returns all medicines in the catalog")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Medicines fetched successfully")
    })
    public ApiResponse<List<MedicineResponse>> getMedicines() {
        return ApiResponse.success("Medicines fetched successfully", inventoryService.getMedicines());
    }

    @PostMapping("/batches")
    @Operation(summary = "Add inventory batch", description = "Adds a stock batch for a center and medicine")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Inventory batch added successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    public ResponseEntity<ApiResponse<InventoryBatchResponse>> addBatch(@Valid @RequestBody CreateInventoryBatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inventory batch added successfully", inventoryService.addBatch(request)));
    }

    @GetMapping("/centers/{centerId}/stock")
    @Operation(summary = "Get center stock", description = "Returns current stock summary for a center")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Current stock fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Center not found")
    })
    public ApiResponse<List<CenterStockResponse>> getCenterStock(@PathVariable Long centerId) {
        return ApiResponse.success("Current stock fetched successfully", inventoryService.getCenterStock(centerId));
    }

    @GetMapping("/centers/{centerId}/batches")
    @Operation(summary = "Get center batches", description = "Returns inventory batches for a center")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Inventory batches fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Center not found")
    })
    public ApiResponse<List<InventoryBatchResponse>> getCenterBatches(@PathVariable Long centerId) {
        return ApiResponse.success("Inventory batches fetched successfully", inventoryService.getCenterBatches(centerId));
    }

    @GetMapping("/centers/{centerId}/expiring")
    @Operation(summary = "Get expiring batches", description = "Returns batches that expire within the provided number of days")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Expiring batches fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Center not found")
    })
    public ApiResponse<List<InventoryBatchResponse>> getExpiringBatches(
            @PathVariable Long centerId,
            @RequestParam(defaultValue = "30") int days
    ) {
        return ApiResponse.success("Expiring batches fetched successfully", inventoryService.getExpiringBatches(centerId, days));
    }
}
