package com.sehati.auth.service;

public interface EmailService {
    void sendVerificationEmail(String toEmail, String otpCode);
    void sendPasswordResetEmail(String toEmail, String otpCode);
    void sendSecretaireInvitationEmail(String toEmail, String medecinName, String token, boolean hasPassword);
    void sendAccountActivatedEmail(String toEmail);
    void sendAccountDeactivatedEmail(String toEmail);
    void sendRequestApprovedEmail(String toEmail);
    void sendRequestRejectedEmail(String toEmail);
}
