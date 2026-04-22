package com.apanaswastha.erp.controller;

import com.apanaswastha.erp.dto.response.commission.CommissionLedgerResponse;
import com.apanaswastha.erp.dto.response.common.ApiResponse;
import com.apanaswastha.erp.service.CommissionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/commissions")
public class CommissionController {

    private final CommissionService commissionService;

    public CommissionController(CommissionService commissionService) {
        this.commissionService = commissionService;
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<CommissionLedgerResponse>> getUserCommissions(@PathVariable Long userId) {
        return ApiResponse.success("Commissions fetched successfully", commissionService.getUserCommissions(userId));
    }
}
