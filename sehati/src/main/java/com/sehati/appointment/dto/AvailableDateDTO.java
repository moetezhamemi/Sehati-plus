package com.sehati.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableDateDTO {
    private String dayName;
    private int dayNumber;
    private String month;
    private String fullDate;
    private boolean disabled;
}
