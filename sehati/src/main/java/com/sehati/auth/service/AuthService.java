package com.sehati.auth.service;

import com.sehati.auth.dto.ForgotPasswordRequest;
import com.sehati.auth.dto.GoogleAuthRequest;
import com.sehati.auth.dto.JwtResponse;
import com.sehati.auth.dto.LoginRequest;
import com.sehati.auth.dto.MessageResponse;
import com.sehati.auth.dto.SignupLaboRequest;
import com.sehati.auth.dto.SignupMedecinRequest;
import com.sehati.auth.dto.SignupPatientRequest;
import com.sehati.auth.dto.ResetPasswordOtpRequest;

public interface AuthService {
    JwtResponse authenticateUser(LoginRequest loginRequest);
    MessageResponse registerPatient(SignupPatientRequest signUpRequest);
    MessageResponse registerMedecin(SignupMedecinRequest signUpRequest);
    MessageResponse registerLaboratoire(SignupLaboRequest signUpRequest);
    
    MessageResponse sendOtp(String email, String purpose);
    java.util.Map<String, Object> verifyEmailOtp(String email, String otp);
    MessageResponse resendVerificationEmail(String email);
    
    MessageResponse forgotPassword(ForgotPasswordRequest request);
    MessageResponse resetPassword(ResetPasswordOtpRequest request);
    
    JwtResponse loginWithGoogle(GoogleAuthRequest request);
}
