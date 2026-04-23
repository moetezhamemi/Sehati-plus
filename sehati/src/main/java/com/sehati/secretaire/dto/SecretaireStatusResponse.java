package com.sehati.secretaire.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Retourné après login d'une secrétaire pour indiquer son état d'accès.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecretaireStatusResponse {
    /**
     * ACTIVE   : Secrétaire est active chez un médecin
     * REMOVED  : Secrétaire a été retirée de tous les médecins
     * NONE     : Aucune relation trouvée
     * PENDING  : Invitation envoyée mais mot de passe pas encore défini
     */
    private String status;
    private String medecinNom;
}
