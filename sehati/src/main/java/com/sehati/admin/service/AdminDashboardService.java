package com.sehati.admin.service;

import com.sehati.admin.dto.AdminDashboardStatsDTO;
import com.sehati.admin.dto.AdminDashboardOverviewDTO;
import com.sehati.admin.dto.AdminGrowthDataDTO;
import com.sehati.admin.dto.AdminQualityStatsDTO;

public interface AdminDashboardService {

    /** Endpoint unifié : stats + quality + pending en 1 appel */
    AdminDashboardOverviewDTO getOverview();

    AdminDashboardStatsDTO getGlobalStats();

    AdminGrowthDataDTO getGrowthData(int year, Integer month);

    AdminQualityStatsDTO getQualityStats();
}
