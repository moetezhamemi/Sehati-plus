package com.sehati.admin.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminSecretaireDetailDTO extends AdminUserDetailDTO {
    private String nom;
    private String prenom;
    private String associatedDoctorName;
    private LocalDateTime associatedSince;
}
