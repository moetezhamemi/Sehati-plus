package com.sehati.patient.controller;

import com.sehati.auth.security.UserDetailsImpl;
import com.sehati.patient.dto.PatientProfileDTO;
import com.sehati.patient.service.PatientService;
import com.sehati.common.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientProfileController {

    private final PatientService patientService;
    private final CloudinaryService cloudinaryService;

    @GetMapping("/profile/me")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<PatientProfileDTO> getMyProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(patientService.getPatientProfile(userDetails.getId()));
    }

    @PutMapping("/profile/me")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<PatientProfileDTO> updateMyProfile(
            @RequestBody PatientProfileDTO dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(patientService.updatePatientProfile(userDetails.getId(), dto));
    }

    @PostMapping("/profile/photo")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<Map<String, String>> uploadProfilePhoto(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        String url = cloudinaryService.uploadFile(file);
        // Persist the URL in the patient record
        patientService.updatePatientPhoto(userDetails.getId(), url);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
