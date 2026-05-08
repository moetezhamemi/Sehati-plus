package com.sehati.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponseDTO {
    private Long id;
    private Long medecinId;
    private Long laboratoireId;
    private Long patientId;
    private List<String> analysesNames;
    private String ordonnanceUrl;
    private LocalDate date;
    private LocalTime time;
    private String status;
}
