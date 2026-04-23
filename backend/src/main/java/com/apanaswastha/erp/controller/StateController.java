package com.apanaswastha.erp.controller;

import com.apanaswastha.erp.dto.request.geography.CreateStateRequest;
import com.apanaswastha.erp.dto.response.geography.StateResponse;
import com.apanaswastha.erp.dto.response.common.ApiResponse;
import com.apanaswastha.erp.service.StateService;
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
@RequestMapping("/api/v1/states")
public class StateController {

    private final StateService stateService;

    public StateController(StateService stateService) {
        this.stateService = stateService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StateResponse>> create(@Valid @RequestBody CreateStateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("State created successfully", stateService.create(request)));
    }

    @GetMapping
    public ApiResponse<List<StateResponse>> getAll() {
        return ApiResponse.success("States fetched successfully", stateService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<StateResponse> getById(@PathVariable Long id) {
        return ApiResponse.success("State fetched successfully", stateService.getById(id));
    }
}
