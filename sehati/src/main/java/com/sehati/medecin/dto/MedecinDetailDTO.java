package com.sehati.medecin.dto;

import com.sehati.common.dto.WorkHoursDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedecinDetailDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String specialite;
    private String adresseCabinet;
    private String ville;
    private Double latitude;
    private Double longitude;
    private List<String> phones;
    private String photoProfilUrl;
    private String biographie;
    private WorkHoursDTO workHours;
    private Double averageRating;
    private Long reviewCount;
}
