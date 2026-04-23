package com.sehati.appointment.entities;

import com.sehati.medecin.entities.Medecin;
import com.sehati.patient.entities.Patient;
import com.sehati.laboratoire.entities.Laboratoire;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medecin_id")
    private Medecin medecin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laboratoire_id")
    private Laboratoire laboratoire;

    private String analysesNames;

    @Column(length = 1000)
    private String ordonnanceUrl;

    @Column(length = 1000)
    private String resultUrl;

    private LocalDate date;
    private LocalTime time;
    private String status; // CONFIRMED, CANCELLED

    @Column(length = 2000)
    private String consultationNotes;

    @Column(length = 1000)
    private String demandeAnalyseUrl;

    @Column(columnDefinition = "boolean default false")
    private boolean deletedByMedecin = false;
}
