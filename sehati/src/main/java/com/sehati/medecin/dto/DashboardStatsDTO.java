package com.sehati.medecin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {
    private long appointmentCount;
    private long consultationCount;
    private long completedAppointments;
    private long confirmedAppointments;
    private long cancelledAppointments;
    private List<TrendPointDTO> appointmentTrend;
    private long totalPatients;
    private Double averageRating;
    private long reviewCount;
    private List<DashboardReviewDTO> recentReviews;
}
