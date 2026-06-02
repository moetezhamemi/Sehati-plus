package com.sehati.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStatsDTO {

    private long totalUsers;
    private long totalMedecins;
    private long totalLaboratoires;
    private long totalPatients;

    private long totalAppointments;
    private long appointmentsThisMonth;
    private long appointmentsLastMonth;

    /** Tendance RDV : % d'évolution vs mois précédent (null si pas de données) */
    private Double appointmentsTrend;

    private double globalCancellationRate;
}
