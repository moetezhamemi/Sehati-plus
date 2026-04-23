package com.sehati.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponseDTO {
    private Long id;
    private Long medecinId;
    private Long laboratoireId;
    private Long patientId;
    private String analysesNames;
    private String ordonnanceUrl;
    private LocalDate date;
    private LocalTime time;
    private String status;
}
