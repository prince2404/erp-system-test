package com.apanaswastha.erp.controller;

import com.apanaswastha.erp.dto.request.family.CreateFamilyMemberRequest;
import com.apanaswastha.erp.dto.request.family.CreateFamilyRequest;
import com.apanaswastha.erp.dto.response.family.FamilyMemberResponse;
import com.apanaswastha.erp.dto.response.family.FamilyResponse;
import com.apanaswastha.erp.dto.response.common.ApiResponse;
import com.apanaswastha.erp.service.FamilyService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/families")
@Tag(name = "Patients", description = "Patient and family management APIs")
public class FamilyController {

    private final FamilyService familyService;

    public FamilyController(FamilyService familyService) {
        this.familyService = familyService;
    }

    @PostMapping
    @Operation(summary = "Register patient family", description = "Registers a new patient family record")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Family registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    public ResponseEntity<ApiResponse<FamilyResponse>> registerFamily(@Valid @RequestBody CreateFamilyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Family registered successfully", familyService.registerFamily(request)));
    }

    @GetMapping
    @Operation(summary = "List patient families", description = "Returns all registered patient families")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Families fetched successfully")
    })
    public ApiResponse<List<FamilyResponse>> listFamilies() {
        return ApiResponse.success("Families fetched successfully", familyService.getAllFamilies());
    }

    @PostMapping("/{healthCardNumber}/members")
    @Operation(summary = "Add family member", description = "Adds a member to an existing patient family")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Family member added successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Family not found")
    })
    public ResponseEntity<ApiResponse<FamilyMemberResponse>> addMember(
            @PathVariable String healthCardNumber,
            @Valid @RequestBody CreateFamilyMemberRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Family member added successfully", familyService.addFamilyMember(healthCardNumber, request)));
    }

    @GetMapping("/{healthCardNumber}")
    @Operation(summary = "Get family by health card number", description = "Fetches patient family details using health card number")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Family fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Family not found")
    })
    public ApiResponse<FamilyResponse> getByHealthCardNumber(@PathVariable String healthCardNumber) {
        return ApiResponse.success("Family fetched successfully", familyService.getByHealthCardNumber(healthCardNumber));
    }
}
