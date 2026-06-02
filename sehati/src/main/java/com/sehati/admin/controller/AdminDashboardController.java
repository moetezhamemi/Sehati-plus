package com.sehati.admin.controller;

import com.sehati.admin.dto.AdminDashboardStatsDTO;
import com.sehati.admin.dto.AdminDashboardOverviewDTO;
import com.sehati.admin.dto.AdminGrowthDataDTO;
import com.sehati.admin.dto.AdminQualityStatsDTO;
import com.sehati.admin.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    /**
     * GET /api/admin/dashboard/stats
     * Retourne les indicateurs globaux (cartes KPI) du tableau de bord.
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AdminDashboardStatsDTO> getGlobalStats() {
        return ResponseEntity.ok(adminDashboardService.getGlobalStats());
    }

    /**
     * GET /api/admin/dashboard/growth?year=2025
     * GET /api/admin/dashboard/growth?year=2025&month=5
     * Retourne les données de croissance filtrables par année et mois optionnel.
     */
    @GetMapping("/growth")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AdminGrowthDataDTO> getGrowthData(
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(required = false) Integer month) {
        if (year == 0) {
            year = LocalDate.now().getYear();
        }
        return ResponseEntity.ok(adminDashboardService.getGrowthData(year, month));
    }

    /**
     * GET /api/admin/dashboard/quality
     * Retourne la répartition des rendez-vous par statut (qualité du service).
     */
    @GetMapping("/quality")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AdminQualityStatsDTO> getQualityStats() {
        return ResponseEntity.ok(adminDashboardService.getQualityStats());
    }

    /**
     * GET /api/admin/dashboard/overview
     * Endpoint unifié : retourne stats (avec tendances) + quality + pending en 1 seul appel.
     * Remplace les 3 appels séparés /stats, /quality et un futur /pending.
     */
    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AdminDashboardOverviewDTO> getOverview() {
        return ResponseEntity.ok(adminDashboardService.getOverview());
    }
}
