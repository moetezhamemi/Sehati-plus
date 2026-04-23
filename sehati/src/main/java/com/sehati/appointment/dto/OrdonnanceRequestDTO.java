package com.sehati.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdonnanceRequestDTO {
    private List<MedicamentDTO> medicaments;
}
