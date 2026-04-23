package com.sehati.laboratoire.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LaboSummaryDTO {
    private Long id;
    private String nomLabo;
    private String adresseComplete;
    private String ville;
    private List<String> phones;
    private String photoProfilUrl;
    private Double averageRating;
    private Long reviewCount;
}
