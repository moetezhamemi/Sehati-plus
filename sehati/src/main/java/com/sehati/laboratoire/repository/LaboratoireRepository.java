package com.sehati.laboratoire.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sehati.laboratoire.entities.Laboratoire;

@Repository
public interface LaboratoireRepository extends JpaRepository<Laboratoire, Long> {
}
