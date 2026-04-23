package com.apanaswastha.erp.controller;

import com.apanaswastha.erp.dto.response.geography.CenterResponse;
import com.apanaswastha.erp.dto.request.geography.CreateCenterRequest;
import com.apanaswastha.erp.dto.response.common.ApiResponse;
import com.apanaswastha.erp.service.CenterService;
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
@RequestMapping("/api/v1/centers")
public class CenterController {

    private final CenterService centerService;

    public CenterController(CenterService centerService) {
        this.centerService = centerService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CenterResponse>> create(@Valid @RequestBody CreateCenterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Center created successfully", centerService.create(request)));
    }

    @GetMapping
    public ApiResponse<List<CenterResponse>> getAll() {
        return ApiResponse.success("Centers fetched successfully", centerService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<CenterResponse> getById(@PathVariable Long id) {
        return ApiResponse.success("Center fetched successfully", centerService.getById(id));
    }
}
