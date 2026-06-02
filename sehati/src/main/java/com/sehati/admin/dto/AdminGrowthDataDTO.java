package com.sehati.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminGrowthDataDTO {

    // Évolution des RDV chez les médecins (par mois ou par jour)
    private List<AdminTrendPoint> medecinAppointments;

    // Évolution des RDV chez les laboratoires (par mois ou par jour)
    private List<AdminTrendPoint> laboAppointments;

    // Nouvelles inscriptions par mois
    private List<AdminTrendPoint> newPatients;
    private List<AdminTrendPoint> newMedecins;
    private List<AdminTrendPoint> newLabos;
}
