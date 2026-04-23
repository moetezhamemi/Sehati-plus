package com.sehati.patient.dto;

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
public class ConsultationItemDTO {
    private Long id;
    private LocalDate date;
    private LocalTime time;
    private String consultationNotes;
    private String ordonnanceUrl;
    private String demandeAnalyseUrl;
}
