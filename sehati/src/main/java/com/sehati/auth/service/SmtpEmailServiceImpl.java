package com.sehati.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
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
    @Async
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
                    + "<h1>Sehati+</h1>"
                    + "</div>"
                    + "<div class=\"content\">"
                    + "<h2>Confirmez votre adresse e-mail</h2>"
                    + "<p>Bienvenue sur Sehati+ ! Nous sommes ravis de vous compter parmi nous.</p>"
                    + "<p>Pour activer votre compte, veuillez saisir le code de vérification suivant dans l'application :</p>"
                    + "<div class=\"otp-box\">" + otpCode + "</div>"
                    + "<p>Ce code expirera dans <strong>10 minutes</strong>.</p>"
                    + "</div>"
                    + "</div>"
                    + "<div class=\"footer\">"
                    + "<p>Si vous n'avez pas créé de compte sur Sehati+, vous pouvez ignorer cet e-mail en toute sécurité.</p>"
                    + "<p>© 2026 Sehati+. Tous droits réservés.</p>"
                    + "</div>"
                    + "</div>"
                    + "</body></html>";

            helper.setFrom("sehati.nepasrepondre@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Sehati+ : Activation de votre compte");
            helper.setText(htmlMsg, true);

            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new BusinessException("Erreur lors de l'envoi de l'e-mail de vérification: " + e.getMessage());
        }
    }

    @Override
    @Async
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
                    + "<h1>Sehati+</h1>"
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
                    + "<p>© 2026 Sehati+. Tous droits réservés.</p>"
                    + "</div>"
                    + "</div>"
                    + "</body></html>";

            helper.setFrom("sehati.nepasrepondre@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Sehati+ : Réinitialisation de votre mot de passe");
            helper.setText(htmlMsg, true);

            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new BusinessException("Erreur lors de l'envoi de l'e-mail de réinitialisation: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void sendSecretaireInvitationEmail(String toEmail, String medecinName, String token, boolean hasPassword) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String setupUrl = "http://localhost:4200/secretaire-setup?token=" + token;
            String buttonText = hasPassword ? "Accepter l'invitation" : "Configurer mon compte";
            String actionText = hasPassword
                    ? "Pour accepter cette invitation et accéder à son espace, veuillez cliquer sur le bouton ci-dessous :"
                    : "Pour activer votre compte et créer votre mot de passe, veuillez cliquer sur le bouton ci-dessous :";

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
                    + "<h1>Sehati+</h1>"
                    + "</div>"
                    + "<div class=\"content\">"
                    + "<h2>Invitation de " + medecinName + "</h2>"
                    + "<p>Bonjour,</p>"
                    + "<p>Le <strong>Dr. " + medecinName
                    + "</strong> vous a invité(e) à rejoindre son cabinet sur la plateforme Sehati+ en tant que secrétaire.</p>"
                    + "<p>" + actionText + "</p>"
                    + "<div class=\"btn-box\">"
                    + "<a href=\"" + setupUrl + "\" class=\"btn\">" + buttonText + "</a>"
                    + "</div>"
                    + "<p>Ce lien est exclusif et confidentiel.</p>"
                    + "</div>"
                    + "<div class=\"footer\">"
                    + "<p>© 2026 Sehati+. Tous droits réservés.</p>"
                    + "</div>"
                    + "</div>"
                    + "</body></html>";

            helper.setFrom("sehati.nepasrepondre@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Sehati+ : Invitation à rejoindre le cabinet du Dr. " + medecinName);
            helper.setText(htmlMsg, true);

            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new BusinessException("Erreur lors de l'envoi de l'e-mail d'invitation: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void sendAccountActivatedEmail(String toEmail) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlMsg = "<!DOCTYPE html>"
                    + "<html><head><meta charset=\"utf-8\"><style>"
                    + "body { font-family: 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f4f7f6; margin: 0; padding: 0; }"
                    + ".container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 8px 20px rgba(0,0,0,0.05); border: 1px solid #eef0f2; }"
                    + ".header { background-color: #10b981; padding: 35px 30px; text-align: center; color: white; }"
                    + ".header h1 { margin: 0; font-size: 28px; font-weight: 700; letter-spacing: -0.5px; }"
                    + ".content { padding: 40px 30px; color: #4b5563; line-height: 1.6; text-align: left; }"
                    + ".content h2 { margin-top: 0; color: #111827; font-size: 22px; font-weight: 600; }"
                    + ".footer { background-color: #f9fafb; padding: 25px; text-align: center; font-size: 13px; color: #9ca3af; border-top: 1px solid #f3f4f6; }"
                    + "</style></head><body>"
                    + "<div class=\"container\">"
                    + "<div class=\"header\">"
                    + "<h1>Sehati+</h1>"
                    + "</div>"
                    + "<div class=\"content\">"
                    + "<h2>Votre compte a été activé</h2>"
                    + "<p>Bonjour,</p>"
                    + "<p>Nous avons le plaisir de vous informer que votre compte sur Sehati+ a été activé par l'administrateur.</p>"
                    + "<p>Vous pouvez dès à présent vous connecter et accéder à tous nos services normalement.</p>"
                    + "<p>Merci de votre confiance.</p>"
                    + "</div>"
                    + "<div class=\"footer\">"
                    + "<p>© 2026 Sehati+. Tous droits réservés.</p>"
                    + "</div>"
                    + "</div>"
                    + "</body></html>";

            helper.setFrom("sehati.nepasrepondre@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Sehati+ : Votre compte a été réactivé");
            helper.setText(htmlMsg, true);

            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new BusinessException("Erreur lors de l'envoi de l'e-mail d'activation: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void sendAccountDeactivatedEmail(String toEmail) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlMsg = "<!DOCTYPE html>"
                    + "<html><head><meta charset=\"utf-8\"><style>"
                    + "body { font-family: 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f4f7f6; margin: 0; padding: 0; }"
                    + ".container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 8px 20px rgba(0,0,0,0.05); border: 1px solid #eef0f2; }"
                    + ".header { background-color: #ef4444; padding: 35px 30px; text-align: center; color: white; }"
                    + ".header h1 { margin: 0; font-size: 28px; font-weight: 700; letter-spacing: -0.5px; }"
                    + ".content { padding: 40px 30px; color: #4b5563; line-height: 1.6; text-align: left; }"
                    + ".content h2 { margin-top: 0; color: #111827; font-size: 22px; font-weight: 600; }"
                    + ".footer { background-color: #f9fafb; padding: 25px; text-align: center; font-size: 13px; color: #9ca3af; border-top: 1px solid #f3f4f6; }"
                    + "</style></head><body>"
                    + "<div class=\"container\">"
                    + "<div class=\"header\">"
                    + "<h1>Sehati+</h1>"
                    + "</div>"
                    + "<div class=\"content\">"
                    + "<h2>Votre compte a été désactivé</h2>"
                    + "<p>Bonjour,</p>"
                    + "<p>Nous vous informons que votre compte sur Sehati+ a été désactivé par l'administrateur.</p>"
                    + "<p>Par conséquent, vous ne pouvez plus vous connecter à la plateforme pour le moment.</p>"
                    + "<p>Si vous pensez qu'il s'agit d'une erreur ou si vous souhaitez obtenir plus d'informations, veuillez contacter notre support.</p>"
                    + "</div>"
                    + "<div class=\"footer\">"
                    + "<p>© 2026 Sehati+. Tous droits réservés.</p>"
                    + "</div>"
                    + "</div>"
                    + "</body></html>";

            helper.setFrom("sehati.nepasrepondre@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Sehati+ : Information concernant votre compte");
            helper.setText(htmlMsg, true);

            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new BusinessException("Erreur lors de l'envoi de l'e-mail de désactivation: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void sendRequestApprovedEmail(String toEmail) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlMsg = "<!DOCTYPE html>"
                    + "<html><head><meta charset=\"utf-8\"><style>"
                    + "body { font-family: 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f4f7f6; margin: 0; padding: 0; }"
                    + ".container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 8px 20px rgba(0,0,0,0.05); border: 1px solid #eef0f2; }"
                    + ".header { background-color: #10b981; padding: 35px 30px; text-align: center; color: white; }"
                    + ".header h1 { margin: 0; font-size: 28px; font-weight: 700; letter-spacing: -0.5px; }"
                    + ".content { padding: 40px 30px; color: #4b5563; line-height: 1.6; text-align: left; }"
                    + ".content h2 { margin-top: 0; color: #111827; font-size: 22px; font-weight: 600; }"
                    + ".footer { background-color: #f9fafb; padding: 25px; text-align: center; font-size: 13px; color: #9ca3af; border-top: 1px solid #f3f4f6; }"
                    + "</style></head><body>"
                    + "<div class=\"container\">"
                    + "<div class=\"header\">"
                    + "<h1>Sehati+</h1>"
                    + "</div>"
                    + "<div class=\"content\">"
                    + "<h2>Votre demande d'inscription a été approuvée</h2>"
                    + "<p>Bonjour,</p>"
                    + "<p>Nous avons le plaisir de vous informer que votre demande d'inscription sur Sehati+ a été <strong>approuvée</strong> par notre équipe d'administration.</p>"
                    + "<p>Votre compte est désormais actif. Vous pouvez dès à présent vous connecter et accéder à tous les services de la plateforme.</p>"
                    + "<p>Merci de votre confiance et bienvenue sur Sehati+ !</p>"
                    + "</div>"
                    + "<div class=\"footer\">"
                    + "<p>© 2026 Sehati+. Tous droits réservés.</p>"
                    + "</div>"
                    + "</div>"
                    + "</body></html>";

            helper.setFrom("sehati.nepasrepondre@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Sehati+ : Votre demande d'inscription a été approuvée");
            helper.setText(htmlMsg, true);

            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new BusinessException("Erreur lors de l'envoi de l'e-mail d'approbation: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void sendRequestRejectedEmail(String toEmail) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlMsg = "<!DOCTYPE html>"
                    + "<html><head><meta charset=\"utf-8\"><style>"
                    + "body { font-family: 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f4f7f6; margin: 0; padding: 0; }"
                    + ".container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 8px 20px rgba(0,0,0,0.05); border: 1px solid #eef0f2; }"
                    + ".header { background-color: #ef4444; padding: 35px 30px; text-align: center; color: white; }"
                    + ".header h1 { margin: 0; font-size: 28px; font-weight: 700; letter-spacing: -0.5px; }"
                    + ".content { padding: 40px 30px; color: #4b5563; line-height: 1.6; text-align: left; }"
                    + ".content h2 { margin-top: 0; color: #111827; font-size: 22px; font-weight: 600; }"
                    + ".footer { background-color: #f9fafb; padding: 25px; text-align: center; font-size: 13px; color: #9ca3af; border-top: 1px solid #f3f4f6; }"
                    + "</style></head><body>"
                    + "<div class=\"container\">"
                    + "<div class=\"header\">"
                    + "<h1>Sehati+</h1>"
                    + "</div>"
                    + "<div class=\"content\">"
                    + "<h2>Votre demande d'inscription a été refusée</h2>"
                    + "<p>Bonjour,</p>"
                    + "<p>Nous regrettons de vous informer que votre demande d'inscription sur Sehati+ a été <strong>refusée</strong> par notre équipe d'administration.</p>"
                    + "<p>Si vous pensez qu'il s'agit d'une erreur ou si vous souhaitez obtenir plus d'informations, veuillez contacter notre support.</p>"
                    + "</div>"
                    + "<div class=\"footer\">"
                    + "<p>© 2026 Sehati+. Tous droits réservés.</p>"
                    + "</div>"
                    + "</div>"
                    + "</body></html>";

            helper.setFrom("sehati.nepasrepondre@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Sehati+ : Votre demande d'inscription a été refusée");
            helper.setText(htmlMsg, true);

            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new BusinessException("Erreur lors de l'envoi de l'e-mail de rejet: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void sendSupportReplyEmail(String toEmail, String originalSubject, String replyText) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlMsg = "<!DOCTYPE html>"
                    + "<html><head><meta charset=\"utf-8\"><style>"
                    + "body { font-family: 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f4f7f6; margin: 0; padding: 0; }"
                    + ".container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 8px 20px rgba(0,0,0,0.05); border: 1px solid #eef0f2; }"
                    + ".header { background-color: #2b5a9e; padding: 35px 30px; text-align: center; color: white; }"
                    + ".header h1 { margin: 0; font-size: 28px; font-weight: 700; letter-spacing: -0.5px; }"
                    + ".content { padding: 40px 30px; color: #4b5563; line-height: 1.6; text-align: left; }"
                    + ".content h2 { margin-top: 0; color: #111827; font-size: 22px; font-weight: 600; }"
                    + ".reply-box { background-color: #f8f9fa; color: #333; font-size: 16px; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #2b5a9e; white-space: pre-wrap; }"
                    + ".footer { background-color: #f9fafb; padding: 25px; text-align: center; font-size: 13px; color: #9ca3af; border-top: 1px solid #f3f4f6; }"
                    + "</style></head><body>"
                    + "<div class=\"container\">"
                    + "<div class=\"header\">"
                    + "<h1>Sehati+ Support</h1>"
                    + "</div>"
                    + "<div class=\"content\">"
                    + "<h2>Réponse à votre message : " + originalSubject + "</h2>"
                    + "<p>Bonjour,</p>"
                    + "<p>Suite à votre récent message envoyé via notre formulaire de contact, voici la réponse de notre équipe support :</p>"
                    + "<div class=\"reply-box\">" + replyText + "</div>"
                    + "<p>Nous espérons avoir répondu à votre demande. N'hésitez pas à nous recontacter si vous avez d'autres questions.</p>"
                    + "<p>L'équipe Sehati+</p>"
                    + "</div>"
                    + "<div class=\"footer\">"
                    + "<p>© 2026 Sehati+. Tous droits réservés.</p>"
                    + "</div>"
                    + "</div>"
                    + "</body></html>";

            helper.setFrom("sehati.nepasrepondre@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Sehati+ Support : Réponse - " + originalSubject);
            helper.setText(htmlMsg, true);

            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new BusinessException("Erreur lors de l'envoi de l'e-mail de support: " + e.getMessage());
        }
    }
}
