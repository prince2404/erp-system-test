package com.apanaswastha.erp.controller;

import com.apanaswastha.erp.dto.CreateDistrictRequest;
import com.apanaswastha.erp.dto.DistrictResponse;
import com.apanaswastha.erp.payload.ApiResponse;
import com.apanaswastha.erp.service.DistrictService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/districts")
public class DistrictController {

    private final DistrictService districtService;

    public DistrictController(DistrictService districtService) {
        this.districtService = districtService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DistrictResponse>> create(@Valid @RequestBody CreateDistrictRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("District created successfully", districtService.create(request)));
    }

    @GetMapping
    public ApiResponse<List<DistrictResponse>> getAll() {
        return ApiResponse.success("Districts fetched successfully", districtService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<DistrictResponse> getById(@PathVariable Long id) {
        return ApiResponse.success("District fetched successfully", districtService.getById(id));
    }
}
