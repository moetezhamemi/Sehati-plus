package com.sehati.patient.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sehati.patient.entities.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    java.util.Optional<Patient> findByUserId(Long userId);

    java.util.Optional<Patient> findFirstByTelephone(String telephone);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Patient p WHERE LOWER(CONCAT(p.nom, ' ', p.prenom)) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(CONCAT(p.prenom, ' ', p.nom)) LIKE LOWER(CONCAT('%', :query, '%'))")
    java.util.List<Patient> searchByFullName(@org.springframework.data.repository.query.Param("query") String query);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT a.patient FROM Appointment a WHERE a.medecin.id = :medecinId AND a.status = 'COMPLETED' AND a.deletedByMedecin = false " +
           "AND (:search IS NULL OR LOWER(CONCAT(a.patient.nom, ' ', a.patient.prenom)) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(CONCAT(a.patient.prenom, ' ', a.patient.nom)) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR a.patient.telephone LIKE CONCAT('%', :search, '%'))")
    java.util.List<Patient> findDistinctPatientsByMedecinId(@org.springframework.data.repository.query.Param("medecinId") Long medecinId, @org.springframework.data.repository.query.Param("search") String search);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT a.patient FROM Appointment a WHERE a.laboratoire.id = :laboId AND a.status = 'COMPLETED' AND a.deletedByMedecin = false " +
           "AND (:search IS NULL OR LOWER(CONCAT(a.patient.nom, ' ', a.patient.prenom)) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(CONCAT(a.patient.prenom, ' ', a.patient.nom)) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR a.patient.telephone LIKE CONCAT('%', :search, '%'))")
    java.util.List<Patient> findDistinctPatientsByLaboratoireId(@org.springframework.data.repository.query.Param("laboId") Long laboId, @org.springframework.data.repository.query.Param("search") String search);
}
