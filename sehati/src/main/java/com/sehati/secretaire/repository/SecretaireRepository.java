package com.sehati.secretaire.repository;

import com.sehati.secretaire.entities.Secretaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SecretaireRepository extends JpaRepository<Secretaire, Long> {

    Optional<Secretaire> findByUserId(Long userId);

    Optional<Secretaire> findByUserEmail(String email);
}
