package com.sehati.medecin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sehati.medecin.entities.Medecin;

@Repository
public interface MedecinRepository extends JpaRepository<Medecin, Long> {
}
