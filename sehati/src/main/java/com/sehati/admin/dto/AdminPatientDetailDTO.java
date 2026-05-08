package com.sehati.admin.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminPatientDetailDTO extends AdminUserDetailDTO {
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String telephone;
}
