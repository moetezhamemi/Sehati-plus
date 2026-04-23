package com.sehati.secretaire.repository;

import com.sehati.secretaire.entities.MedecinSecretaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedecinSecretaireRepository extends JpaRepository<MedecinSecretaire, Long> {

    /**
     * Trouver la relation active ou en attente d'un médecin
     */
    Optional<MedecinSecretaire> findByMedecinIdAndStatusIn(Long medecinId, List<String> statuses);

    /**
     * Trouver une relation spécifique entre un médecin et une secrétaire
     */
    Optional<MedecinSecretaire> findByMedecinIdAndSecretaireId(Long medecinId, Long secretaireId);

    /**
     * Trouver toutes les relations d'une secrétaire (pour vérifier son accès)
     */
    List<MedecinSecretaire> findBySecretaireUserId(Long userId);

    /**
     * Vérifier si un médecin a déjà une secrétaire avec un de ces statuts
     */
    boolean existsByMedecinIdAndStatusIn(Long medecinId, List<String> statuses);

    /**
     * Trouver la relation PENDING d'une secrétaire (pour activation après password setup)
     */
    Optional<MedecinSecretaire> findBySecretaireUserIdAndStatus(Long userId, String status);
}
