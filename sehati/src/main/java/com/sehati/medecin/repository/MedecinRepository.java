package com.sehati.medecin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sehati.medecin.entities.Medecin;

import java.util.Optional;

@Repository
public interface MedecinRepository extends JpaRepository<Medecin, Long> {

    @Query("SELECT m FROM Medecin m JOIN m.user u WHERE u.enabled = true AND u.status = 'APPROVED'")
    Page<Medecin> findAllApprovedAndEnabled(Pageable pageable);

    @Query("SELECT m FROM Medecin m JOIN m.user u WHERE u.enabled = true AND u.status = 'APPROVED' " +
           "AND (:search IS NULL OR LOWER(m.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(m.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(CONCAT(m.nom, ' ', m.prenom)) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:ville IS NULL OR LOWER(m.ville) = LOWER(:ville)) " +
           "AND (:specialite IS NULL OR LOWER(m.specialite) = LOWER(:specialite))")
    Page<Medecin> searchApproved(@org.springframework.data.repository.query.Param("search") String search, 
                                 @org.springframework.data.repository.query.Param("ville") String ville, 
                                 @org.springframework.data.repository.query.Param("specialite") String specialite, 
                                 Pageable pageable);

    @Query("SELECT DISTINCT m.ville FROM Medecin m JOIN m.user u WHERE u.enabled = true AND u.status = 'APPROVED' AND m.ville IS NOT NULL ORDER BY m.ville")
    java.util.List<String> findDistinctVilles();

    @Query("SELECT DISTINCT m.specialite FROM Medecin m JOIN m.user u WHERE u.enabled = true AND u.status = 'APPROVED' AND m.specialite IS NOT NULL ORDER BY m.specialite")
    java.util.List<String> findDistinctSpecialites();

    Optional<Medecin> findByUserId(Long userId);
}
