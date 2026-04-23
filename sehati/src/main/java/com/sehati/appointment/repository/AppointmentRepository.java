package com.sehati.appointment.repository;

import com.sehati.appointment.entities.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // --- Patient ---
    List<Appointment> findByPatientIdAndDateAfterOrderByDateAscTimeAsc(Long patientId, LocalDate date);
    Page<Appointment> findByPatientIdAndStatusAndDateBeforeOrderByDateDescTimeDesc(Long patientId, String status, LocalDate date, Pageable pageable);

    // --- Médecin : vue liste (par date) ---
    List<Appointment> findByMedecinIdAndDateOrderByTimeAsc(Long medecinId, LocalDate date);

    // --- Médecin : vue emploi du temps (plage de dates) ---
    List<Appointment> findByMedecinIdAndDateBetweenOrderByDateAscTimeAsc(Long medecinId, LocalDate start, LocalDate end);

    // --- Laboratoire : vue liste (par date) ---
    List<Appointment> findByLaboratoireIdAndDateOrderByTimeAsc(Long laboratoireId, LocalDate date);

    // --- Laboratoire : vue emploi du temps (plage de dates) ---
    List<Appointment> findByLaboratoireIdAndDateBetweenOrderByDateAscTimeAsc(Long laboratoireId, LocalDate start, LocalDate end);

    // --- Méthodes legacy conservées (utilisées dans AppointmentService existant) ---
    List<Appointment> findByMedecinIdAndDateAndStatus(Long medecinId, LocalDate date, String status);
    List<Appointment> findByLaboratoireIdAndDateAndStatus(Long laboratoireId, LocalDate date, String status);

    // --- Dossier Patient ---
    List<Appointment> findByPatientIdAndMedecinIdAndStatusAndDeletedByMedecinFalseOrderByDateDescTimeDesc(Long patientId, Long medecinId, String status);
    List<Appointment> findByPatientIdAndMedecinId(Long patientId, Long medecinId);
}
