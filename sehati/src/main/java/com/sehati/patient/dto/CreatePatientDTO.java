package com.sehati.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreatePatientDTO {
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotNull(message = "La date de naissance est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateNaissance;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(regexp = "^\\d{8}$", message = "Le numéro doit contenir uniquement 8 chiffres")
    private String telephone;
}
