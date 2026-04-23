package com.sehati.laboratoire.repository;

import com.sehati.laboratoire.entities.Laboratoire;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LaboratoireRepository extends JpaRepository<Laboratoire, Long> {

    @Query("SELECT l FROM Laboratoire l JOIN l.user u WHERE u.enabled = true AND u.status = 'APPROVED'")
    Page<Laboratoire> findAllApprovedAndEnabled(Pageable pageable);

    @Query("SELECT l FROM Laboratoire l JOIN l.user u WHERE u.enabled = true AND u.status = 'APPROVED' " +
           "AND (:search IS NULL OR LOWER(l.nomLabo) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:ville IS NULL OR LOWER(l.ville) = LOWER(:ville))")
    Page<Laboratoire> searchApproved(@org.springframework.data.repository.query.Param("search") String search, 
                                     @org.springframework.data.repository.query.Param("ville") String ville, 
                                     Pageable pageable);

    Optional<Laboratoire> findByUserId(Long userId);
}
