package com.sehati.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientHistoryDTO {
    private Long patientId;
    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private Integer age;
    private String pdpUrl;
    private LocalDate derniereVisite;
    private List<ConsultationItemDTO> consultations;
}
