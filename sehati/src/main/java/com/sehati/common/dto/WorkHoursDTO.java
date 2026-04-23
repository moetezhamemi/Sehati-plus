package com.sehati.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkHoursDTO {
    private DayScheduleDTO lundi;
    private DayScheduleDTO mardi;
    private DayScheduleDTO mercredi;
    private DayScheduleDTO jeudi;
    private DayScheduleDTO vendredi;
    private DayScheduleDTO samedi;
    private DayScheduleDTO dimanche;
}
