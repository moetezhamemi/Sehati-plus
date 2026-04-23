package com.sehati.laboratoire.dto;

import com.sehati.common.dto.WorkHoursDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaboHoraireDTO {
    private Integer consultationTime;
    private Integer capaciteParCreneau;
    private WorkHoursDTO workHours;
}
