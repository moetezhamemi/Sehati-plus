package com.sehati.patient.controller;

import com.sehati.auth.security.UserDetailsImpl;
import com.sehati.patient.dto.CreatePatientDTO;
import com.sehati.patient.dto.ProfessionalPatientDTO;
import com.sehati.patient.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/mes-patients")
    @PreAuthorize("hasAnyAuthority('MEDECIN', 'SECRETAIRE', 'LABORATOIRE')")
    public ResponseEntity<List<ProfessionalPatientDTO>> getMyPatients(
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.equals("MEDECIN") || auth.equals("SECRETAIRE") || auth.equals("LABORATOIRE"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Role not allowed"));

        return ResponseEntity.ok(patientService.getMyPatients(userDetails.getId(), role, search));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyAuthority('MEDECIN', 'SECRETAIRE')")
    public ResponseEntity<com.sehati.patient.dto.PatientHistoryDTO> getPatientHistory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.equals("MEDECIN") || auth.equals("SECRETAIRE"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Role not allowed"));

        return ResponseEntity.ok(patientService.getPatientHistoryForMedecin(id, userDetails.getId(), role));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('MEDECIN', 'SECRETAIRE')")
    public ResponseEntity<Void> removePatientFromWorkspace(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.equals("MEDECIN") || auth.equals("SECRETAIRE"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Role not allowed"));

        patientService.removePatientFromMedecinWorkspace(id, userDetails.getId(), role);
        return ResponseEntity.noContent().build();
    }

    // =========================================================
    // Recherche & Création manuelle de patient
    // =========================================================

    @GetMapping("/search-by-phone")
    @PreAuthorize("hasAnyAuthority('MEDECIN', 'SECRETAIRE')")
    public ResponseEntity<ProfessionalPatientDTO> searchByPhone(
            @RequestParam String telephone) {
        return patientService.searchByTelephone(telephone)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/search-by-name")
    @PreAuthorize("hasAnyAuthority('MEDECIN', 'SECRETAIRE')")
    public ResponseEntity<List<ProfessionalPatientDTO>> searchByName(
            @RequestParam String query) {
        return ResponseEntity.ok(patientService.searchByFullName(query));
    }

    @PostMapping("/create-manual")
    @PreAuthorize("hasAnyAuthority('MEDECIN', 'SECRETAIRE')")
    public ResponseEntity<ProfessionalPatientDTO> createPatientManually(
            @Valid @RequestBody CreatePatientDTO dto) {
        return new ResponseEntity<>(patientService.createPatientManually(dto), HttpStatus.CREATED);
    }

}

