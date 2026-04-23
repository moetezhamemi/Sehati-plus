package com.sehati.laboratoire.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaboProfileDTO {
    private String nomLabo;
    private String responsable;
    private String email;
    private String photoProfilUrl;
    private String adresseComplete;
    private String ville;
    private List<String> phones;
    private Double latitude;
    private Double longitude;
    private List<String> analyses;
}
