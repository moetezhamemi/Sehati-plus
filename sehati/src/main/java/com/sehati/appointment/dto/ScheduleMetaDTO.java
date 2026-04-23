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
public class ScheduleMetaDTO {

    /** Durée d'un créneau en minutes (= consultation_time du professionnel) */
    private int slotDurationMinutes;

    /** Heure de début la plus tôt parmi tous les jours travaillés (format "HH:mm") */
    private String earliestStart;

    /** Heure de fin la plus tardive parmi tous les jours travaillés (format "HH:mm") */
    private String latestEnd;

    /** Jours de semaine actifs (ex: ["MONDAY","TUESDAY","WEDNESDAY"]) */
    private List<String> workingDays;
}
