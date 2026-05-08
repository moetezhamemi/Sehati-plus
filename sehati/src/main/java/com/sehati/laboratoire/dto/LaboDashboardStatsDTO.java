package com.sehati.laboratoire.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LaboDashboardStatsDTO {
    private long appointmentCount;
    private long completedAppointments;
    private long confirmedAppointments;
    private long cancelledAppointments;
    private List<LaboTrendPointDTO> appointmentTrend;
    private long totalPatients;
    private Double averageRating;
    private long reviewCount;
    private List<LaboDashboardReviewDTO> recentReviews;
}
