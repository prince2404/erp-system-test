package com.apanaswastha.erp.dto.response.dashboard;

import java.math.BigDecimal;

public class DashboardMetricsDTO {

    private final BigDecimal totalRevenue;
    private final long totalActiveCenters;
    private final BigDecimal totalPendingCommissions;
    private final long dailyOpdVisits;

    public DashboardMetricsDTO(
            BigDecimal totalRevenue,
            long totalActiveCenters,
            BigDecimal totalPendingCommissions,
            long dailyOpdVisits
    ) {
        this.totalRevenue = totalRevenue;
        this.totalActiveCenters = totalActiveCenters;
        this.totalPendingCommissions = totalPendingCommissions;
        this.dailyOpdVisits = dailyOpdVisits;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public long getTotalActiveCenters() {
        return totalActiveCenters;
    }

    public BigDecimal getTotalPendingCommissions() {
        return totalPendingCommissions;
    }

    public long getDailyOpdVisits() {
        return dailyOpdVisits;
    }
}
