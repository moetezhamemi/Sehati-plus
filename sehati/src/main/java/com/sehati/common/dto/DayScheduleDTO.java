package com.sehati.common.dto;

import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayScheduleDTO {
    private LocalTime debut;
    private LocalTime fin;
    private boolean ferme;
}