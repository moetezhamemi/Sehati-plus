package com.sehati.medecin.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedecinProfileDTO {
    private String nom;
    private String prenom;
    private String email;
    private String specialite;
    private String biographie;
    private String photoProfilUrl;
    private String signatureUrl;
    private String cachetUrl;
    private String adresseCabinet;
    private String ville;
    private List<String> phones;
    private Double latitude;
    private Double longitude;
}
