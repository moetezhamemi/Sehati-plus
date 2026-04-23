package com.sehati.laboratoire.dto;

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
public class LaboDetailDTO {
    private Long id;
    private String nomLabo;
    private String adresseComplete;
    private String ville;
    private Double latitude;
    private Double longitude;
    private List<String> phones;
    private String email;
    private List<String> analyses;
    private String photoProfilUrl;
    private String responsable;
    private WorkHoursDTO workHours;
    private Double averageRating;
    private Long reviewCount;
}
