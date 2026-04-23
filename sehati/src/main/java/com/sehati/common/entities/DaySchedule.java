package com.sehati.common.entities;

import jakarta.persistence.Embeddable;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class DaySchedule {
    private LocalTime debut;
    private LocalTime fin;
    private boolean ferme;
}
