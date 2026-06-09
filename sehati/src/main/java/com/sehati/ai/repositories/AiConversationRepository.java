package com.sehati.ai.repositories;

import com.sehati.ai.entities.AiConversation;
import com.sehati.patient.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {
    List<AiConversation> findByPatientOrderByStartedAtDesc(Patient patient);
}
