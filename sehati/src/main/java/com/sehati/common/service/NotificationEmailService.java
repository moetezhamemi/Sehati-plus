package com.sehati.common.service;

import com.sehati.appointment.entities.Appointment;
import com.sehati.laboratoire.entities.Laboratoire;
import com.sehati.medecin.entities.Medecin;
import com.sehati.patient.entities.Patient;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class NotificationEmailService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEmailService.class);

    private final JavaMailSender emailSender;

    @Value("${sehati.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    private static final String FROM_EMAIL = "sahhati.plus@gmail.com";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // =========================================================
    // 1. Rappel de rendez-vous (24h ou 2h avant)
    // =========================================================

    public void sendAppointmentReminder(Appointment appointment, String reminderType) {
        Patient patient = appointment.getPatient();
        if (patient == null || patient.getUser() == null || patient.getUser().getEmail() == null) {
            logger.warn("Cannot send reminder: patient or email is null for appointment {}", appointment.getId());
            return;
        }

        String toEmail = patient.getUser().getEmail();
        String providerName = getProviderName(appointment);
        String address = getProviderAddress(appointment);
        String dateFormatted = appointment.getDate().format(DATE_FMT);
        String timeFormatted = appointment.getTime().format(TIME_FMT);

        String delayText = "24h".equals(reminderType) ? "dans 24 heures" : "dans 2 heures";
        String subject = "Sehhati+ : Rappel de votre rendez-vous " + delayText;

        String htmlMsg = buildEmailLayout(
                "Rappel de rendez-vous",
                "<p>Bonjour <strong>" + patient.getPrenom() + "</strong>,</p>"
                + "<p>Nous vous rappelons que vous avez un rendez-vous <strong>" + delayText + "</strong> :</p>"
                + buildInfoCard(providerName, dateFormatted, timeFormatted, address)
                + "<p style=\"margin-top:20px;color:#6b7280;\">Nous vous souhaitons une excellente consultation.</p>",
                null, null
        );

        sendEmail(toEmail, subject, htmlMsg, "reminder " + reminderType, appointment.getId());
    }

    // =========================================================
    // 2. Annulation de rendez-vous par le professionnel
    // =========================================================

    public void sendAppointmentCancellation(Appointment appointment) {
        Patient patient = appointment.getPatient();
        if (patient == null || patient.getUser() == null || patient.getUser().getEmail() == null) {
            logger.warn("Cannot send cancellation: patient or email is null for appointment {}", appointment.getId());
            return;
        }

        String toEmail = patient.getUser().getEmail();
        String providerName = getProviderName(appointment);
        String address = getProviderAddress(appointment);
        String dateFormatted = appointment.getDate().format(DATE_FMT);
        String timeFormatted = appointment.getTime().format(TIME_FMT);

        String subject = "Sehhati+ : Votre rendez-vous a été annulé";

        String htmlMsg = buildEmailLayout(
                "Rendez-vous annulé",
                "<p>Bonjour <strong>" + patient.getPrenom() + "</strong>,</p>"
                + "<p>Nous vous informons que votre rendez-vous a été <strong style=\"color:#ef4444;\">annulé</strong> par le professionnel de santé.</p>"
                + buildInfoCard(providerName, dateFormatted, timeFormatted, address),
                "Voir mes rendez-vous",
                frontendBaseUrl + "/patient/appointments"
        );

        sendEmail(toEmail, subject, htmlMsg, "cancellation", appointment.getId());
    }

    // =========================================================
    // 3. Résultats d'analyses disponibles
    // =========================================================

    public void sendResultsAvailable(Appointment appointment) {
        Patient patient = appointment.getPatient();
        if (patient == null || patient.getUser() == null || patient.getUser().getEmail() == null) {
            logger.warn("Cannot send results notification: patient or email is null for appointment {}", appointment.getId());
            return;
        }

        String toEmail = patient.getUser().getEmail();
        String labName = appointment.getLaboratoire() != null ? appointment.getLaboratoire().getNomLabo() : "Votre laboratoire";
        String dateFormatted = appointment.getDate().format(DATE_FMT);

        String subject = "Sehhati+ : Vos résultats d'analyses sont disponibles";

        String htmlMsg = buildEmailLayout(
                "Résultats disponibles",
                "<p>Bonjour <strong>" + patient.getPrenom() + "</strong>,</p>"
                + "<p>Bonne nouvelle ! Vos résultats d'analyses effectuées chez <strong>" + labName + "</strong> le <strong>" + dateFormatted + "</strong> sont désormais disponibles sur la plateforme.</p>"
                + "<p style=\"color:#6b7280;\">Vous pouvez les consulter à tout moment depuis votre historique médical.</p>",
                "Consulter mes résultats",
                frontendBaseUrl + "/patient/history"
        );

        sendEmail(toEmail, subject, htmlMsg, "results available", appointment.getId());
    }

    // =========================================================
    // Méthodes utilitaires privées
    // =========================================================

    private String getProviderName(Appointment appointment) {
        if (appointment.getMedecin() != null) {
            Medecin m = appointment.getMedecin();
            return "Dr " + m.getPrenom() + " " + m.getNom();
        } else if (appointment.getLaboratoire() != null) {
            return appointment.getLaboratoire().getNomLabo();
        }
        return "Professionnel de santé";
    }

    private String getProviderAddress(Appointment appointment) {
        if (appointment.getMedecin() != null) {
            Medecin m = appointment.getMedecin();
            return m.getAdresseCabinet() != null ? m.getAdresseCabinet() : m.getVille();
        } else if (appointment.getLaboratoire() != null) {
            Laboratoire l = appointment.getLaboratoire();
            return l.getAdresseComplete() != null ? l.getAdresseComplete() : l.getVille();
        }
        return "";
    }

    private String buildInfoCard(String providerName, String date, String time, String address) {
        return "<div style=\"background-color:#f0f9ff;border:1px solid #bae6fd;border-radius:10px;padding:20px;margin:20px 0;\">"
                + "<table cellpadding='0' cellspacing='0' style='width:100%;'>"
                + "<tr><td style='padding:6px 0;color:#64748b;font-size:13px;width:110px;'>👨‍⚕️ Professionnel</td>"
                + "<td style='padding:6px 0;font-weight:600;color:#1e293b;font-size:14px;'>" + providerName + "</td></tr>"
                + "<tr><td style='padding:6px 0;color:#64748b;font-size:13px;'>📅 Date</td>"
                + "<td style='padding:6px 0;font-weight:600;color:#1e293b;font-size:14px;'>" + date + "</td></tr>"
                + "<tr><td style='padding:6px 0;color:#64748b;font-size:13px;'>🕐 Heure</td>"
                + "<td style='padding:6px 0;font-weight:600;color:#1e293b;font-size:14px;'>" + time + "</td></tr>"
                + "<tr><td style='padding:6px 0;color:#64748b;font-size:13px;'>📍 Adresse</td>"
                + "<td style='padding:6px 0;font-weight:600;color:#1e293b;font-size:14px;'>" + address + "</td></tr>"
                + "</table></div>";
    }

    /**
     * Construit le layout HTML commun à tous les emails de notification.
     * Le header utilise un bleu (#2563eb) comme demandé.
     */
    private String buildEmailLayout(String title, String bodyContent, String ctaText, String ctaUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>")
          .append("<html><head><meta charset=\"utf-8\"><style>")
          .append("body { font-family: 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f4f7f6; margin: 0; padding: 0; }")
          .append(".container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 8px 20px rgba(0,0,0,0.05); border: 1px solid #eef0f2; }")
          .append(".header { background-color: #2563eb; padding: 35px 30px; text-align: center; color: white; }")
          .append(".header h1 { margin: 0 0 6px 0; font-size: 28px; font-weight: 700; letter-spacing: -0.5px; }")
          .append(".header p { margin: 0; font-size: 15px; opacity: 0.9; }")
          .append(".content { padding: 40px 30px; color: #4b5563; line-height: 1.7; text-align: left; font-size: 15px; }")
          .append(".content h2 { margin-top: 0; color: #111827; font-size: 22px; font-weight: 600; }")
          .append(".btn-box { text-align: center; margin: 30px 0; }")
          .append(".btn { background-color: #2563eb; color: white !important; font-size: 16px; font-weight: 600; padding: 15px 30px; border-radius: 8px; text-decoration: none; display: inline-block; }")
          .append(".footer { background-color: #f9fafb; padding: 25px; text-align: center; font-size: 13px; color: #9ca3af; border-top: 1px solid #f3f4f6; }")
          .append("</style></head><body>")
          .append("<div class=\"container\">")
          .append("<div class=\"header\">")
          .append("<h1>Sehhati+</h1>")
          .append("<p>").append(title).append("</p>")
          .append("</div>")
          .append("<div class=\"content\">")
          .append(bodyContent);

        if (ctaText != null && ctaUrl != null) {
            sb.append("<div class=\"btn-box\">")
              .append("<a href=\"").append(ctaUrl).append("\" class=\"btn\">").append(ctaText).append("</a>")
              .append("</div>");
        }

        sb.append("</div>")
          .append("<div class=\"footer\">")
          .append("<p>Cet e-mail a été envoyé automatiquement par la plateforme Sehhati+.</p>")
          .append("<p>© 2026 Sehhati+. Tous droits réservés.</p>")
          .append("</div>")
          .append("</div>")
          .append("</body></html>");

        return sb.toString();
    }

    private void sendEmail(String toEmail, String subject, String htmlContent, String type, Long appointmentId) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom(FROM_EMAIL);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            emailSender.send(mimeMessage);
            logger.info("Notification email [{}] sent to {} for appointment {}", type, toEmail, appointmentId);
        } catch (MessagingException e) {
            logger.error("Failed to send notification email [{}] to {} for appointment {}: {}", type, toEmail, appointmentId, e.getMessage());
        }
    }
}
