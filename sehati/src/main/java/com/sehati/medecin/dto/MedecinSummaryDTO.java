package com.sehati.medecin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedecinSummaryDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String specialite;
    private String adresseCabinet;
    private String ville;
    private List<String> phones;
    private String photoProfilUrl;
    private Double averageRating;
    private Long reviewCount;
}
