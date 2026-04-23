package com.sehati.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandeAnalyseRequestDTO {
    private List<AnalyseItemDTO> analyses;
}
