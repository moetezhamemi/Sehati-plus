package com.sehati.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCityStatsDTO {
    private String ville;
    private long medecinsCount;
    private long labosCount;
}
