package com.sehati.appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ManualAppointmentRequestDTO {
    private Long patientId;
    private com.sehati.patient.dto.CreatePatientDTO newPatient;

    @NotNull(message = "La date est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @NotNull(message = "L'heure est obligatoire")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime time;
}
