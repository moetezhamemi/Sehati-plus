package com.sehati.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminQualityStatsDTO {

    private long completedCount;
    private long confirmedCount;
    private long cancelledCount;
    private long totalCount;

    private double completedPercent;
    private double confirmedPercent;
    private double cancelledPercent;
}
