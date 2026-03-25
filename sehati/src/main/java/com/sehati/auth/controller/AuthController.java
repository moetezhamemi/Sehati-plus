package com.sehati.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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
import com.sehati.auth.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest));
    }

    @PostMapping("/signup/patient")
    public ResponseEntity<?> registerPatient(@Valid @RequestBody SignupPatientRequest signUpRequest) {
        return ResponseEntity.ok(authService.registerPatient(signUpRequest));
    }

    @PostMapping("/signup/medecin")
    public ResponseEntity<?> registerMedecin(@Valid @RequestBody SignupMedecinRequest signUpRequest) {
        return ResponseEntity.ok(authService.registerMedecin(signUpRequest));
    }

    @PostMapping("/signup/labo")
    public ResponseEntity<?> registerLaboratoire(@Valid @RequestBody SignupLaboRequest signUpRequest) {
        return ResponseEntity.ok(authService.registerLaboratoire(signUpRequest));
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(authService.sendOtp(request.getEmail(), request.getPurpose()));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(authService.verifyEmailOtp(request.getEmail(), request.getOtp()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordOtpRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody java.util.Map<String, String> body) {
        return ResponseEntity.ok(authService.resendVerificationEmail(body.get("email")));
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@Valid @RequestBody GoogleAuthRequest request) {
        return ResponseEntity.ok(authService.loginWithGoogle(request));
    }
}
