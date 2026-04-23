package com.sehati.secretaire.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecretaireResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private Boolean enabled;
    private String relationStatus; // PENDING | ACTIVE
}
