package com.sehati.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientAppointmentDTO {
    private Long id;
    private Long providerId;
    private String providerName;   // Nom du médecin ou du laboratoire
    private String type;           // "Consultation" ou "Analyse"
    private String specialite;     // Spécialité si médecin, null si labo
    private LocalDateTime dateTime;
    private String address;
    private String status;
    private String resultUrl;
    private String providerPdpUrl;     // URL de la photo de profil
    private String ordonnanceUrl;      // L'ordonnance associée
    private String demandeAnalyseUrl;  // La demande d'analyse associée
}
