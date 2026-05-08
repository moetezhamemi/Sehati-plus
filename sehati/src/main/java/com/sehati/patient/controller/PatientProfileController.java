package com.sehati.patient.controller;

import com.sehati.auth.security.UserDetailsImpl;
import com.sehati.common.dto.ApiResponse;
import com.sehati.common.exception.PhoneVerificationRequiredException;
import com.sehati.common.service.TwilioSmsService;
import com.sehati.patient.dto.PatientProfileDTO;
import com.sehati.patient.service.PatientService;
import com.sehati.common.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientProfileController {

    private static final Logger logger = LoggerFactory.getLogger(PatientProfileController.class);

    private final PatientService patientService;
    private final CloudinaryService cloudinaryService;
    private final TwilioSmsService twilioSmsService;

    @GetMapping("/profile/me")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<PatientProfileDTO> getMyProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(patientService.getPatientProfile(userDetails.getId()));
    }

    @PutMapping("/profile/me")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<?> updateMyProfile(
            @RequestBody PatientProfileDTO dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            PatientProfileDTO updated = patientService.updatePatientProfile(userDetails.getId(), dto);
            return ResponseEntity.ok(updated);
        } catch (PhoneVerificationRequiredException e) {
            // Un dossier orphelin existe → le frontend doit lancer la vérification SMS
            Map<String, Object> data = new HashMap<>();
            data.put("requiresPhoneVerification", true);
            data.put("orphanPatientId", e.getOrphanPatientId());
            data.put("message", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error(e.getMessage(), data));
        }
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

    // =========================================================
    // Vérification téléphone depuis le profil (Flux B)
    // =========================================================

    @PostMapping("/profile/send-phone-otp")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<ApiResponse<Void>> sendPhoneOtp(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String telephone = body.get("telephone");
        if (telephone == null || telephone.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Le numéro de téléphone est obligatoire."));
        }

        try {
            twilioSmsService.sendVerificationCode(telephone);
            logger.info("Phone OTP sent from profile for user {}", userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("Un code de vérification a été envoyé par SMS.", null));
        } catch (Exception e) {
            logger.error("Failed to send phone OTP for user {}", userDetails.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erreur lors de l'envoi du SMS. Veuillez réessayer."));
        }
    }

    @PostMapping("/profile/verify-phone-otp")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<ApiResponse<?>> verifyPhoneOtp(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String telephone = body.get("telephone");
        String code = body.get("code");
        String orphanPatientIdStr = body.get("orphanPatientId");

        if (telephone == null || code == null || orphanPatientIdStr == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Données manquantes."));
        }

        boolean approved;
        try {
            approved = twilioSmsService.verifyCode(telephone, code);
        } catch (Exception e) {
            logger.error("Twilio verification failed for phone {}", telephone, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Erreur lors de la vérification du code SMS."));
        }

        if (!approved) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Code SMS incorrect ou expiré."));
        }

        Long orphanPatientId = Long.parseLong(orphanPatientIdStr);
        PatientProfileDTO updatedProfile = patientService.mergePatientOnPhoneUpdate(
                userDetails.getId(), orphanPatientId);

        logger.info("Profile phone merge completed for user {}", userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Dossier médical fusionné avec succès.", updatedProfile));
    }
}
