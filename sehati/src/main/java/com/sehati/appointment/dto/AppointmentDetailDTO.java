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
public class AppointmentDetailDTO {
    private Long id;
    private Long patientId;
    private String patientNom;
    private String patientPrenom;
    private String patientPdpUrl;
    private String patientTelephone;
    private LocalDate date;
    private LocalTime time;
    private String status;
    private String consultationNotes;
    private String analysesNames;
    private String ordonnanceUrl;
    private String resultUrl;
    private boolean hasHistory;
}
