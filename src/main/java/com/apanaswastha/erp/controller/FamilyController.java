package com.apanaswastha.erp.controller;

import com.apanaswastha.erp.dto.CreateFamilyMemberRequest;
import com.apanaswastha.erp.dto.CreateFamilyRequest;
import com.apanaswastha.erp.dto.FamilyMemberResponse;
import com.apanaswastha.erp.dto.FamilyResponse;
import com.apanaswastha.erp.payload.ApiResponse;
import com.apanaswastha.erp.service.FamilyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/families")
public class FamilyController {

    private final FamilyService familyService;

    public FamilyController(FamilyService familyService) {
        this.familyService = familyService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FamilyResponse>> registerFamily(@Valid @RequestBody CreateFamilyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Family registered successfully", familyService.registerFamily(request)));
    }

    @PostMapping("/{healthCardNumber}/members")
    public ResponseEntity<ApiResponse<FamilyMemberResponse>> addMember(
            @PathVariable String healthCardNumber,
            @Valid @RequestBody CreateFamilyMemberRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Family member added successfully", familyService.addFamilyMember(healthCardNumber, request)));
    }

    @GetMapping("/{healthCardNumber}")
    public ApiResponse<FamilyResponse> getByHealthCardNumber(@PathVariable String healthCardNumber) {
        return ApiResponse.success("Family fetched successfully", familyService.getByHealthCardNumber(healthCardNumber));
    }
}
