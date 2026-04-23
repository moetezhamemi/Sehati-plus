package com.sehati.auth.service;

import com.sehati.auth.dto.ForgotPasswordRequest;
import com.sehati.auth.dto.GoogleAuthRequest;
import com.sehati.auth.dto.JwtResponse;
import com.sehati.auth.dto.LoginRequest;
import com.sehati.auth.dto.SignupLaboRequest;
import com.sehati.auth.dto.SignupMedecinRequest;
import com.sehati.auth.dto.SignupPatientRequest;
import com.sehati.auth.dto.ResetPasswordOtpRequest;

public interface AuthService {
    JwtResponse authenticateUser(LoginRequest loginRequest);
    String registerPatient(SignupPatientRequest signUpRequest);
    String registerMedecin(SignupMedecinRequest signUpRequest);
    String registerLaboratoire(SignupLaboRequest signUpRequest);
    
    String sendOtp(String email, String purpose);
    java.util.Map<String, Object> verifyEmailOtp(String email, String otp);
    String resendVerificationEmail(String email);
    
    String forgotPassword(ForgotPasswordRequest request);
    String resetPassword(ResetPasswordOtpRequest request);
    
    void updatePassword(Long userId, String oldPassword, String newPassword);
    
    com.sehati.auth.dto.SecretaireTokenCheckResponse checkSecretaireToken(String token);

    JwtResponse loginWithGoogle(GoogleAuthRequest request);
    JwtResponse setupSecretairePassword(com.sehati.auth.dto.SecretaireSetupPasswordRequest request);
}
