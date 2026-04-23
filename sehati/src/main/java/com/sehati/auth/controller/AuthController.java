package com.sehati.auth.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.sehati.auth.dto.ForgotPasswordRequest;
import com.sehati.auth.dto.GoogleAuthRequest;
import com.sehati.auth.dto.LoginRequest;
import com.sehati.auth.dto.OtpRequest;
import com.sehati.auth.dto.OtpVerifyRequest;
import com.sehati.auth.dto.ResetPasswordOtpRequest;
import com.sehati.auth.dto.SignupLaboRequest;
import com.sehati.auth.dto.SignupMedecinRequest;
import com.sehati.auth.dto.SignupPatientRequest;
import com.sehati.auth.security.UserDetailsImpl;
import com.sehati.auth.service.AuthService;
import com.sehati.common.dto.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<?>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(ApiResponse.success(authService.authenticateUser(loginRequest)));
    }

    @PostMapping("/signup/patient")
    public ResponseEntity<ApiResponse<Void>> registerPatient(@Valid @RequestBody SignupPatientRequest signUpRequest) {
        return ResponseEntity.ok(ApiResponse.success(authService.registerPatient(signUpRequest), null));
    }

    @PostMapping("/signup/medecin")
    public ResponseEntity<ApiResponse<Void>> registerMedecin(@Valid @RequestBody SignupMedecinRequest signUpRequest) {
        return ResponseEntity.ok(ApiResponse.success(authService.registerMedecin(signUpRequest), null));
    }

    @PostMapping("/signup/labo")
    public ResponseEntity<ApiResponse<Void>> registerLaboratoire(@Valid @RequestBody SignupLaboRequest signUpRequest) {
        return ResponseEntity.ok(ApiResponse.success(authService.registerLaboratoire(signUpRequest), null));
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.sendOtp(request.getEmail(), request.getPurpose()), null));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<?>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Vérification réussie", authService.verifyEmailOtp(request.getEmail(), request.getOtp())));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.forgotPassword(request), null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordOtpRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.resetPassword(request), null));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(authService.resendVerificationEmail(body.get("email")), null));
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<?>> googleLogin(@Valid @RequestBody GoogleAuthRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.loginWithGoogle(request)));
    }

    @PutMapping("/update-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @RequestBody com.sehati.auth.dto.UpdatePasswordDTO dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        authService.updatePassword(userDetails.getId(), dto.getOldPassword(), dto.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Mot de passe mis à jour avec succès.", null));
    }

    @GetMapping("/check-secretaire-token")
    public ResponseEntity<ApiResponse<?>> checkSecretaireToken(@org.springframework.web.bind.annotation.RequestParam String token) {
        return ResponseEntity.ok(ApiResponse.success("Vérification du jeton.", authService.checkSecretaireToken(token)));
    }

    @PostMapping("/setup-secretaire-password")
    public ResponseEntity<ApiResponse<?>> setupSecretairePassword(@Valid @RequestBody com.sehati.auth.dto.SecretaireSetupPasswordRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Mot de passe enregistré avec succès, vous allez être redirigé.", authService.setupSecretairePassword(request)));
    }
}

