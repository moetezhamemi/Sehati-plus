package com.sehati.laboratoire.controller;

import com.sehati.auth.security.UserDetailsImpl;
import com.sehati.common.service.CloudinaryService;
import com.sehati.laboratoire.dto.LaboProfileDTO;
import com.sehati.laboratoire.dto.LaboHoraireDTO;
import com.sehati.laboratoire.service.LaboratoireService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/labo")
@RequiredArgsConstructor
public class LaboProfileController {

    private final LaboratoireService laboratoireService;
    private final CloudinaryService cloudinaryService;

    @GetMapping("/profile/me")
    @PreAuthorize("hasAuthority('LABORATOIRE')")
    public ResponseEntity<LaboProfileDTO> getMyProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(laboratoireService.getLaboProfile(userDetails.getId()));
    }

    @PutMapping("/profile/me")
    @PreAuthorize("hasAuthority('LABORATOIRE')")
    public ResponseEntity<LaboProfileDTO> updateMyProfile(
            @RequestBody LaboProfileDTO dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(laboratoireService.updateLaboProfile(userDetails.getId(), dto));
    }

    @PostMapping("/profile/photo")
    @PreAuthorize("hasAuthority('LABORATOIRE')")
    public ResponseEntity<Map<String, String>> uploadProfilePhoto(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String url = cloudinaryService.uploadFile(file);
        laboratoireService.updateLaboPhoto(userDetails.getId(), url, cloudinaryService);
        return ResponseEntity.ok(Map.of("url", url));
    }

    // ==================== Horaire d'Ouverture ====================

    @GetMapping("/horaire")
    @PreAuthorize("hasAuthority('LABORATOIRE')")
    public ResponseEntity<LaboHoraireDTO> getHoraire(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(laboratoireService.getHoraire(userDetails.getId()));
    }

    @PutMapping("/horaire")
    @PreAuthorize("hasAuthority('LABORATOIRE')")
    public ResponseEntity<LaboHoraireDTO> updateHoraire(
            @RequestBody LaboHoraireDTO dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(laboratoireService.updateHoraire(userDetails.getId(), dto));
    }
}
