package com.sehati.medecin.dto;

import com.sehati.common.dto.WorkHoursDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoraireDTO {
    private Integer consultationTime;
    private WorkHoursDTO workHours;
}
