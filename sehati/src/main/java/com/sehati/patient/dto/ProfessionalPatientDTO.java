package com.sehati.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalPatientDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String telephone;
    private String photoProfilUrl;
    private LocalDate dateNaissance;
}
