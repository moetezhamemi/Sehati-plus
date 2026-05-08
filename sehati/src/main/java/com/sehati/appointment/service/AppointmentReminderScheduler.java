package com.sehati.appointment.service;

import com.sehati.appointment.entities.Appointment;
import com.sehati.appointment.repository.AppointmentRepository;
import com.sehati.common.service.NotificationEmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Tâche planifiée qui vérifie toutes les 60 secondes les rendez-vous
 * nécessitant un rappel par e-mail (24h et 2h avant).
 */
@Component
@RequiredArgsConstructor
public class AppointmentReminderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentReminderScheduler.class);

    private final AppointmentRepository appointmentRepository;
    private final NotificationEmailService notificationEmailService;

    @Scheduled(fixedRate = 60000) // toutes les 60 secondes
    @Transactional
    public void checkAndSendReminders() {
        LocalDateTime now = LocalDateTime.now();

        sendReminders24h(now);
        sendReminders2h(now);
    }

    /**
     * Rappel 24 heures avant le rendez-vous.
     * On cherche les RDV dont la date/heure se situe entre now+23h59m et now+24h01m.
     */
    private void sendReminders24h(LocalDateTime now) {
        LocalDateTime target = now.plusHours(24);
        LocalDate targetDate = target.toLocalDate();
        LocalTime windowStart = target.toLocalTime().minusMinutes(1);
        LocalTime windowEnd = target.toLocalTime().plusMinutes(1);

        // Gérer le cas où le fenêtrage traverse minuit
        if (windowStart.isAfter(windowEnd)) {
            return; // cas rare, on ignore
        }

        List<Appointment> appointments = appointmentRepository
                .findReminder24hCandidates(targetDate, windowStart, windowEnd);

        for (Appointment appointment : appointments) {
            try {
                notificationEmailService.sendAppointmentReminder(appointment, "24h");
                appointment.setReminder24hSent(true);
                appointmentRepository.save(appointment);
                logger.info("24h reminder sent for appointment {}", appointment.getId());
            } catch (Exception e) {
                logger.error("Failed to send 24h reminder for appointment {}: {}", appointment.getId(), e.getMessage());
            }
        }
    }

    /**
     * Rappel 2 heures avant le rendez-vous.
     * On cherche les RDV dont la date/heure se situe entre now+1h59m et now+2h01m.
     */
    private void sendReminders2h(LocalDateTime now) {
        LocalDateTime target = now.plusHours(2);
        LocalDate targetDate = target.toLocalDate();
        LocalTime windowStart = target.toLocalTime().minusMinutes(1);
        LocalTime windowEnd = target.toLocalTime().plusMinutes(1);

        // Gérer le cas où le fenêtrage traverse minuit
        if (windowStart.isAfter(windowEnd)) {
            return; // cas rare, on ignore
        }

        List<Appointment> appointments = appointmentRepository
                .findReminder2hCandidates(targetDate, windowStart, windowEnd);

        for (Appointment appointment : appointments) {
            try {
                notificationEmailService.sendAppointmentReminder(appointment, "2h");
                appointment.setReminder2hSent(true);
                appointmentRepository.save(appointment);
                logger.info("2h reminder sent for appointment {}", appointment.getId());
            } catch (Exception e) {
                logger.error("Failed to send 2h reminder for appointment {}: {}", appointment.getId(), e.getMessage());
            }
        }
    }
}
