package com.sehati.appointment.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequestDTO {
    private Long medecinId;
    private Long laboratoireId;
    private String analysesNames;
    private String ordonnanceUrl;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime time;
}
