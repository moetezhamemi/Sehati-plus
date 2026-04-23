package com.sehati.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.sehati.common.exception.BusinessException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@Primary
public class SmtpEmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Override
    public void sendVerificationEmail(String toEmail, String otpCode) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlMsg = "<!DOCTYPE html>"
                    + "<html><head><meta charset=\"utf-8\"><style>"
                    + "body { font-family: 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f4f7f6; margin: 0; padding: 0; }"
                    + ".container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 8px 20px rgba(0,0,0,0.05); border: 1px solid #eef0f2; }"
                    + ".header { background-color: #818cf8; padding: 35px 30px; text-align: center; color: white; }"
                    + ".header h1 { margin: 0; font-size: 28px; font-weight: 700; letter-spacing: -0.5px; }"
                    + ".content { padding: 40px 30px; color: #4b5563; line-height: 1.6; text-align: left; }"
                    + ".content h2 { margin-top: 0; color: #111827; font-size: 22px; font-weight: 600; }"
                    + ".otp-box { background-color: #f3f4f6; color: #111827; font-size: 32px; font-weight: 700; letter-spacing: 4px; text-align: center; padding: 20px; border-radius: 8px; margin: 30px 0; border: 2px dashed #818cf8; }"
                    + ".footer { background-color: #f9fafb; padding: 25px; text-align: center; font-size: 13px; color: #9ca3af; border-top: 1px solid #f3f4f6; }"
                    + "</style></head><body>"
                    + "<div class=\"container\">"
                    + "<div class=\"header\">"
                    + "<h1>Sehhati+</h1>"
                    + "</div>"
                    + "<div class=\"content\">"
                    + "<h2>Confirmez votre adresse e-mail</h2>"
                    + "<p>Bienvenue sur Sehhati+ ! Nous sommes ravis de vous compter parmi nous.</p>"
                    + "<p>Pour activer votre compte, veuillez saisir le code de vérification suivant dans l'application :</p>"
                    + "<div class=\"otp-box\">" + otpCode + "</div>"
                    + "<p>Ce code expirera dans <strong>10 minutes</strong>.</p>"
                    + "</div>"
                    + "</div>"
                    + "<div class=\"footer\">"
                    + "<p>Si vous n'avez pas créé de compte sur Sehhati+, vous pouvez ignorer cet e-mail en toute sécurité.</p>"
                    + "<p>© 2026 Sehhati+. Tous droits réservés.</p>"
                    + "</div>"
                    + "</div>"
                    + "</body></html>";

            helper.setFrom("sehati.nepasrepondre@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Sehhati+ : Activation de votre compte");
            helper.setText(htmlMsg, true);

            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new BusinessException("Erreur lors de l'envoi de l'e-mail de vérification: " + e.getMessage());
        }
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String otpCode) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlMsg = "<!DOCTYPE html>"
                    + "<html><head><meta charset=\"utf-8\"><style>"
                    + "body { font-family: 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f4f7f6; margin: 0; padding: 0; }"
                    + ".container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 8px 20px rgba(0,0,0,0.05); border: 1px solid #eef0f2; }"
                    + ".header { background-color: #818cf8; padding: 35px 30px; text-align: center; color: white; }"
                    + ".header h1 { margin: 0; font-size: 28px; font-weight: 700; letter-spacing: -0.5px; }"
                    + ".content { padding: 40px 30px; color: #4b5563; line-height: 1.6; text-align: left; }"
                    + ".content h2 { margin-top: 0; color: #111827; font-size: 22px; font-weight: 600; }"
                    + ".otp-box { background-color: #f3f4f6; color: #111827; font-size: 32px; font-weight: 700; letter-spacing: 4px; text-align: center; padding: 20px; border-radius: 8px; margin: 30px 0; border: 2px dashed #818cf8; }"
                    + ".footer { background-color: #f9fafb; padding: 25px; text-align: center; font-size: 13px; color: #9ca3af; border-top: 1px solid #f3f4f6; }"
                    + ".warning { font-size: 14px; color: #6b7280; margin-top: 30px; background-color: #fef2f2; border-left: 4px solid #ef4444; padding: 10px 15px; }"
                    + "</style></head><body>"
                    + "<div class=\"container\">"
                    + "<div class=\"header\">"
                    + "<h1>Sehhati+</h1>"
                    + "</div>"
                    + "<div class=\"content\">"
                    + "<h2>Réinitialisation de votre mot de passe</h2>"
                    + "<p>Bonjour,</p>"
                    + "<p>Nous avons reçu une demande de réinitialisation de mot de passe pour votre compte. "
                    + "Veuillez utiliser le code ci-dessous pour créer un nouveau mot de passe :</p>"
                    + "<div class=\"otp-box\">" + otpCode + "</div>"
                    + "<p>Ce code expirera dans <strong>10 minutes</strong>.</p>"
                    + "<div class=\"warning\">Si vous n'avez pas demandé de réinitialisation, ignorez cet e-mail. Votre mot de passe restera inchangé.</div>"
                    + "</div>"
                    + "<div class=\"footer\">"
                    + "<p>© 2026 Sehhati+. Tous droits réservés.</p>"
                    + "</div>"
                    + "</div>"
                    + "</body></html>";

            helper.setFrom("sehati.nepasrepondre@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Sehhati+ : Réinitialisation de votre mot de passe");
            helper.setText(htmlMsg, true);

            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new BusinessException("Erreur lors de l'envoi de l'e-mail de réinitialisation: " + e.getMessage());
        }
    }

    @Override
    public void sendSecretaireInvitationEmail(String toEmail, String medecinName, String token) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String setupUrl = "http://localhost:4200/secretaire-setup?token=" + token;

            String htmlMsg = "<!DOCTYPE html>"
                    + "<html><head><meta charset=\"utf-8\"><style>"
                    + "body { font-family: 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f4f7f6; margin: 0; padding: 0; }"
                    + ".container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 8px 20px rgba(0,0,0,0.05); border: 1px solid #eef0f2; }"
                    + ".header { background-color: #818cf8; padding: 35px 30px; text-align: center; color: white; }"
                    + ".header h1 { margin: 0; font-size: 28px; font-weight: 700; letter-spacing: -0.5px; }"
                    + ".content { padding: 40px 30px; color: #4b5563; line-height: 1.6; text-align: left; }"
                    + ".content h2 { margin-top: 0; color: #111827; font-size: 22px; font-weight: 600; }"
                    + ".btn-box { text-align: center; margin: 30px 0; }"
                    + ".btn { background-color: #818cf8; color: white !important; font-size: 16px; font-weight: 600; padding: 15px 30px; border-radius: 8px; text-decoration: none; display: inline-block; }"
                    + ".footer { background-color: #f9fafb; padding: 25px; text-align: center; font-size: 13px; color: #9ca3af; border-top: 1px solid #f3f4f6; }"
                    + "</style></head><body>"
                    + "<div class=\"container\">"
                    + "<div class=\"header\">"
                    + "<h1>Sehhati+</h1>"
                    + "</div>"
                    + "<div class=\"content\">"
                    + "<h2>Invitation de " + medecinName + "</h2>"
                    + "<p>Bonjour,</p>"
                    + "<p>Le <strong>Dr. " + medecinName + "</strong> vous a invité(e) à rejoindre son cabinet sur la plateforme Sehhati+ en tant que secrétaire.</p>"
                    + "<p>Pour activer votre compte et créer votre mot de passe, veuillez cliquer sur le bouton ci-dessous :</p>"
                    + "<div class=\"btn-box\">"
                    + "<a href=\"" + setupUrl + "\" class=\"btn\">Configurer mon compte</a>"
                    + "</div>"
                    + "<p>Ce lien est exclusif et confidentiel.</p>"
                    + "</div>"
                    + "<div class=\"footer\">"
                    + "<p>© 2026 Sehhati+. Tous droits réservés.</p>"
                    + "</div>"
                    + "</div>"
                    + "</body></html>";

            helper.setFrom("sehati.nepasrepondre@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Sehhati+ : Invitation à rejoindre le cabinet du Dr. " + medecinName);
            helper.setText(htmlMsg, true);

            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new BusinessException("Erreur lors de l'envoi de l'e-mail d'invitation: " + e.getMessage());
        }
    }
}
