package com.sehati.auth.service;

public interface EmailService {
    void sendVerificationEmail(String toEmail, String otpCode);
    void sendPasswordResetEmail(String toEmail, String otpCode);
    void sendSecretaireInvitationEmail(String toEmail, String medecinName, String token);
}
