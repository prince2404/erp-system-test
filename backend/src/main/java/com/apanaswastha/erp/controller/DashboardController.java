package com.apanaswastha.erp.controller;

import com.apanaswastha.erp.dto.response.dashboard.DashboardMetricsDTO;
import com.apanaswastha.erp.dto.response.common.ApiResponse;
import com.apanaswastha.erp.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/metrics")
    public ApiResponse<DashboardMetricsDTO> getMetrics(Authentication authentication) {
        DashboardMetricsDTO metrics = dashboardService.getMetricsForUser(authentication.getName());
        return ApiResponse.success("Dashboard metrics fetched successfully", metrics);
    }
}
