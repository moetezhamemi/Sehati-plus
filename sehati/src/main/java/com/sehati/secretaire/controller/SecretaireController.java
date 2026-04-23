package com.sehati.secretaire.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sehati.auth.security.UserDetailsImpl;
import com.sehati.common.dto.ApiResponse;
import com.sehati.secretaire.dto.AddSecretaireRequest;
import com.sehati.secretaire.dto.SecretaireResponse;
import com.sehati.secretaire.dto.SecretaireStatusResponse;
import com.sehati.secretaire.service.SecretaireService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SecretaireController {

    private final SecretaireService secretaireService;

    // ==================== Médecin endpoints ====================

    @GetMapping("/api/medecin/secretaire")
    @PreAuthorize("hasAuthority('MEDECIN')")
    public ResponseEntity<ApiResponse<SecretaireResponse>> getSecretaire(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        SecretaireResponse response = secretaireService.getMedecinSecretaire(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/api/medecin/secretaire")
    @PreAuthorize("hasAuthority('MEDECIN')")
    public ResponseEntity<ApiResponse<SecretaireResponse>> inviteSecretaire(
            @Valid @RequestBody AddSecretaireRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        SecretaireResponse response = secretaireService.inviteSecretaire(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Invitation envoyée avec succès", response));
    }

    @DeleteMapping("/api/medecin/secretaire")
    @PreAuthorize("hasAuthority('MEDECIN')")
    public ResponseEntity<ApiResponse<Void>> removeSecretaire(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        secretaireService.removeSecretaire(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Secrétaire retirée avec succès", null));
    }

    // ==================== Secrétaire endpoints ====================

    /**
     * Vérifie le statut d'accès de la secrétaire connectée.
     * Utilisé par le frontend après login pour décider de la redirection.
     */
    @GetMapping("/api/secretaire/status")
    @PreAuthorize("hasAuthority('SECRETAIRE')")
    public ResponseEntity<ApiResponse<SecretaireStatusResponse>> getMyStatus(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        SecretaireStatusResponse status = secretaireService.getSecretaireStatus(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}
