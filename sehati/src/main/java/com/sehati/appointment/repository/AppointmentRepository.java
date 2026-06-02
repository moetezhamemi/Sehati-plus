package com.sehati.appointment.repository;

import com.sehati.appointment.entities.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // --- Patient ---
    List<Appointment> findByPatientIdAndDateAfterOrderByDateAscTimeAsc(Long patientId, LocalDate date);
    Page<Appointment> findByPatientIdAndStatusAndDateBeforeOrderByDateDescTimeDesc(Long patientId, String status, LocalDate date, Pageable pageable);
    Page<Appointment> findByPatientIdAndMedecinIsNotNullAndStatusAndDateBeforeOrderByDateDescTimeDesc(Long patientId, String status, LocalDate date, Pageable pageable);
    Page<Appointment> findByPatientIdAndLaboratoireIsNotNullAndStatusAndDateBeforeOrderByDateDescTimeDesc(Long patientId, String status, LocalDate date, Pageable pageable);

    // --- Patient : recherche historique (filtre type + recherche textuelle) ---
    @Query("SELECT a FROM Appointment a " +
           "LEFT JOIN a.medecin m " +
           "LEFT JOIN a.laboratoire l " +
           "WHERE a.patient.id = :patientId " +
           "AND a.status = :status " +
           "AND a.date < :dateBefore " +
           "AND (:type IS NULL OR " +
           "     (:type = 'Médecin' AND a.medecin IS NOT NULL) OR " +
           "     (:type = 'Laboratoire' AND a.laboratoire IS NOT NULL)) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(CONCAT(COALESCE(m.prenom,''), ' ', COALESCE(m.nom,''))) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(COALESCE(l.nomLabo,'')) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY a.date DESC, a.time DESC")
    Page<Appointment> searchPatientHistory(
            @Param("patientId") Long patientId,
            @Param("status") String status,
            @Param("dateBefore") LocalDate dateBefore,
            @Param("type") String type,
            @Param("search") String search,
            Pageable pageable);

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

    // --- Review eligibility ---
    boolean existsByPatientIdAndMedecinIdAndStatus(Long patientId, Long medecinId, String status);
    boolean existsByPatientIdAndLaboratoireIdAndStatus(Long patientId, Long laboratoireId, String status);

    // --- Dashboard Médecin ---
    long countByMedecinIdAndDateBetween(Long medecinId, LocalDate start, LocalDate end);
    long countByMedecinIdAndStatusAndDateBetween(Long medecinId, String status, LocalDate start, LocalDate end);

    @org.springframework.data.jpa.repository.Query("SELECT a.date, COUNT(a) FROM Appointment a " +
           "WHERE a.medecin.id = :medecinId AND a.date BETWEEN :start AND :end " +
           "GROUP BY a.date ORDER BY a.date")
    List<Object[]> countByMedecinIdGroupByDate(@org.springframework.data.repository.query.Param("medecinId") Long medecinId,
                                               @org.springframework.data.repository.query.Param("start") LocalDate start,
                                               @org.springframework.data.repository.query.Param("end") LocalDate end);

    // --- Dashboard Laboratoire ---
    long countByLaboratoireIdAndDateBetween(Long laboId, LocalDate start, LocalDate end);
    long countByLaboratoireIdAndStatusAndDateBetween(Long laboId, String status, LocalDate start, LocalDate end);

    @org.springframework.data.jpa.repository.Query("SELECT a.date, COUNT(a) FROM Appointment a " +
           "WHERE a.laboratoire.id = :laboId AND a.date BETWEEN :start AND :end " +
           "GROUP BY a.date ORDER BY a.date")
    List<Object[]> countByLaboratoireIdGroupByDate(@org.springframework.data.repository.query.Param("laboId") Long laboId,
                                                   @org.springframework.data.repository.query.Param("start") LocalDate start,
                                                   @org.springframework.data.repository.query.Param("end") LocalDate end);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT a.date FROM Appointment a " +
           "WHERE a.medecin.id = :medecinId AND a.status = 'CONFIRMED' AND a.date < CURRENT_DATE")
    List<LocalDate> findDatesWithPendingPastAppointmentsForMedecin(@org.springframework.data.repository.query.Param("medecinId") Long medecinId);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT a.date FROM Appointment a " +
           "WHERE a.laboratoire.id = :laboId AND a.status = 'CONFIRMED' AND a.date < CURRENT_DATE")
    List<LocalDate> findDatesWithPendingPastAppointmentsForLabo(@org.springframework.data.repository.query.Param("laboId") Long laboId);

    // --- Patient merge ---
    List<Appointment> findByPatientId(Long patientId);

    // --- Notification reminders ---
    @Query("SELECT a FROM Appointment a WHERE a.status = 'CONFIRMED' " +
           "AND a.reminder24hSent = false " +
           "AND a.date = :date AND a.time BETWEEN :startTime AND :endTime")
    List<Appointment> findReminder24hCandidates(
            @Param("date") LocalDate date,
            @Param("startTime") java.time.LocalTime startTime,
            @Param("endTime") java.time.LocalTime endTime);

    @Query("SELECT a FROM Appointment a WHERE a.status = 'CONFIRMED' " +
           "AND a.reminder2hSent = false " +
           "AND a.date = :date AND a.time BETWEEN :startTime AND :endTime")
    List<Appointment> findReminder2hCandidates(
            @Param("date") LocalDate date,
            @Param("startTime") java.time.LocalTime startTime,
            @Param("endTime") java.time.LocalTime endTime);

    // --- Dashboard Admin Global ---

    // Comptage total RDV sur une plage de dates (mois courant)
    long countByDateBetween(LocalDate start, LocalDate end);

    // Comptage global par statut (tous professionnels confondus)
    long countByStatus(String status);

    // Évolution mensuelle RDV médecins pour une année donnée
    @Query("SELECT MONTH(a.date), COUNT(a) FROM Appointment a " +
           "WHERE a.medecin IS NOT NULL AND YEAR(a.date) = :year " +
           "GROUP BY MONTH(a.date) ORDER BY MONTH(a.date)")
    List<Object[]> countMedecinAppointmentsByMonth(@Param("year") int year);

    // Évolution mensuelle RDV laboratoires pour une année donnée
    @Query("SELECT MONTH(a.date), COUNT(a) FROM Appointment a " +
           "WHERE a.laboratoire IS NOT NULL AND YEAR(a.date) = :year " +
           "GROUP BY MONTH(a.date) ORDER BY MONTH(a.date)")
    List<Object[]> countLaboAppointmentsByMonth(@Param("year") int year);

    // Évolution par jour RDV médecins pour un mois/année donné
    @Query("SELECT DAY(a.date), COUNT(a) FROM Appointment a " +
           "WHERE a.medecin IS NOT NULL AND YEAR(a.date) = :year AND MONTH(a.date) = :month " +
           "GROUP BY DAY(a.date) ORDER BY DAY(a.date)")
    List<Object[]> countMedecinAppointmentsByDay(@Param("year") int year, @Param("month") int month);

    // Évolution par jour RDV laboratoires pour un mois/année donné
    @Query("SELECT DAY(a.date), COUNT(a) FROM Appointment a " +
           "WHERE a.laboratoire IS NOT NULL AND YEAR(a.date) = :year AND MONTH(a.date) = :month " +
           "GROUP BY DAY(a.date) ORDER BY DAY(a.date)")
    List<Object[]> countLaboAppointmentsByDay(@Param("year") int year, @Param("month") int month);
}
