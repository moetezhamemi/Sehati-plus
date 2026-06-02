package com.sehati.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardOverviewDTO {

    /** Indicateurs globaux avec tendances */
    private AdminDashboardStatsDTO stats;

    /** Répartition des statuts de rendez-vous */
    private AdminQualityStatsDTO quality;

    /** Demandes d'inscription en attente */
    private AdminPendingRequestsDTO pending;

    /** Répartition par ville des professionnels */
    private java.util.List<AdminCityStatsDTO> citiesDistribution;
}
