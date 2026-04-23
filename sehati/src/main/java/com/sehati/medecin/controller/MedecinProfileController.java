package com.sehati.medecin.controller;

import com.sehati.auth.security.UserDetailsImpl;
import com.sehati.common.service.CloudinaryService;
import com.sehati.medecin.dto.HoraireDTO;
import com.sehati.medecin.dto.MedecinProfileDTO;
import com.sehati.medecin.service.MedecinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/medecin")
@RequiredArgsConstructor
public class MedecinProfileController {

    private final MedecinService medecinService;
    private final CloudinaryService cloudinaryService;

    @GetMapping("/profile/me")
    @PreAuthorize("hasAuthority('MEDECIN')")
    public ResponseEntity<MedecinProfileDTO> getMyProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(medecinService.getMedecinProfile(userDetails.getId()));
    }

    @PutMapping("/profile/me")
    @PreAuthorize("hasAuthority('MEDECIN')")
    public ResponseEntity<MedecinProfileDTO> updateMyProfile(
            @RequestBody MedecinProfileDTO dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(medecinService.updateMedecinProfile(userDetails.getId(), dto));
    }

    @PostMapping("/profile/photo")
    @PreAuthorize("hasAuthority('MEDECIN')")
    public ResponseEntity<Map<String, String>> uploadProfilePhoto(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String url = cloudinaryService.uploadFile(file);
        medecinService.updateMedecinPhoto(userDetails.getId(), url, cloudinaryService);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/profile/signature")
    @PreAuthorize("hasAuthority('MEDECIN')")
    public ResponseEntity<Map<String, String>> uploadSignature(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("error", "La taille du fichier ne doit pas dépasser 5 Mo."));
        }
        String url = cloudinaryService.uploadSignaturePng(file);
        medecinService.updateSignature(userDetails.getId(), url, cloudinaryService);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/profile/cachet")
    @PreAuthorize("hasAuthority('MEDECIN')")
    public ResponseEntity<Map<String, String>> uploadCachet(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("error", "La taille du fichier ne doit pas dépasser 5 Mo."));
        }
        String url = cloudinaryService.uploadCachetWithBgRemoval(file);
        medecinService.updateCachet(userDetails.getId(), url, cloudinaryService);
        return ResponseEntity.ok(Map.of("url", url));
    }

    // ==================== Horaire d'Ouverture ====================

    @GetMapping("/horaire")
    @PreAuthorize("hasAuthority('MEDECIN')")
    public ResponseEntity<HoraireDTO> getHoraire(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(medecinService.getHoraire(userDetails.getId()));
    }

    @PutMapping("/horaire")
    @PreAuthorize("hasAuthority('MEDECIN')")
    public ResponseEntity<HoraireDTO> updateHoraire(
            @RequestBody HoraireDTO dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(medecinService.updateHoraire(userDetails.getId(), dto));
    }
}
