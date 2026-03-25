package com.sehati.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock implementation of EmailService for local development.
 * To use this instead of SMTP, remove @Primary from SmtpEmailServiceImpl
 * and add @Service here (and remove @Service from SmtpEmailServiceImpl).
 */
public class MockEmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(MockEmailServiceImpl.class);

    @Override
    public void sendVerificationEmail(String toEmail, String otpCode) {
        logger.info("===============================================");
        logger.info("Mock Email Sent to: {}", toEmail);
        logger.info("Subject: Sehati+ : Activation de votre compte");
        logger.info("Body: Votre code de vérification est : {}", otpCode);
        logger.info("===============================================");
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String otpCode) {
        logger.info("===============================================");
        logger.info("Mock Email Sent to: {}", toEmail);
        logger.info("Subject: Sehati+ : Réinitialisation de votre mot de passe");
        logger.info("Body: Votre code de réinitialisation est : {}", otpCode);
        logger.info("===============================================");
    }
}
