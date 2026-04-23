package com.sehati.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientProfileDTO {
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String telephone;
    private String email;
    private String photoProfilUrl;
}
