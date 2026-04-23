package com.apanaswastha.erp.service;

import com.apanaswastha.erp.dto.response.dashboard.DashboardMetricsDTO;

public interface DashboardService {

    /**
     * Calculates dashboard metrics for authenticated user.
     *
     * @param username logged-in username
     * @return dashboard metrics snapshot
     */
    DashboardMetricsDTO getMetricsForUser(String username);
}
