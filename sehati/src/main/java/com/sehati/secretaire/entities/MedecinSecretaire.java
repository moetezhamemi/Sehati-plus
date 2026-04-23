package com.sehati.secretaire.entities;

import com.sehati.medecin.entities.Medecin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "medecin_secretaire")
public class MedecinSecretaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "medecin_id", nullable = false)
    private Medecin medecin;

    @ManyToOne
    @JoinColumn(name = "secretaire_id", nullable = false)
    private Secretaire secretaire;

    /**
     * PENDING  : Invitation envoyée, secrétaire n'a pas encore activé son compte
     * ACTIVE   : Secrétaire a configuré son mot de passe et est active
     * REMOVED  : Médecin a retiré la secrétaire (soft remove, aucune donnée supprimée)
     */
    @Column(nullable = false)
    private String status;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
