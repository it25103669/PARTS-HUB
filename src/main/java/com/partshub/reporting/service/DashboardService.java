package com.partshub.reporting.service;

import com.partshub.reporting.dto.DashboardDTO;

/**
 * Contract for supplying all data needed by the main dashboard.
 * <p>
 * Current implementation: {@link MockDashboardDataProvider}
 * Future implementation: DatabaseDashboardDataProvider (connect to real repositories)
 */
public interface DashboardService {

    /**
     * Returns the complete dashboard data object containing
     * summary cards, chart data, and recent activity feed.
     */
    DashboardDTO getDashboardData();
}
