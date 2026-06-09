package com.sehati.ai.tools;

import com.sehati.appointment.entities.Appointment;
import com.sehati.appointment.repository.AppointmentRepository;
import com.sehati.laboratoire.entities.Laboratoire;
import com.sehati.laboratoire.repository.LaboratoireRepository;
import com.sehati.medecin.entities.Medecin;
import com.sehati.medecin.repository.MedecinRepository;
import com.sehati.patient.entities.Patient;
import com.sehati.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class MedicalToolsConfig {

    private final MedecinRepository medecinRepository;
    private final LaboratoireRepository laboRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final com.sehati.appointment.service.AppointmentService appointmentService;

    @Tool(description = "Recherche des médecins par spécialité et/ou par ville. La ville et la spécialité sont totalement optionnelles. IMPORTANT: NE PASSE JAMAIS null. Si tu n'as pas de ville ou de spécialité, passe une chaîne vide \"\". " +
            "ASTUCE: Pour la spécialité, utilise la racine du mot (ex: 'urolog' au lieu de 'urologue' ou 'urologie', 'dent' au lieu de 'dentiste') pour maximiser les correspondances en base de données.")
    public DoctorSearchResponse searchNearbyDoctors(@Nullable String ville, @Nullable String specialite) {
        String finalVille = (ville != null && ville.trim().isEmpty()) ? null : ville;
        String finalSpecialite = (specialite != null && specialite.trim().isEmpty()) ? null : specialite;

        List<Medecin> medecins = medecinRepository.searchApproved(
                null, finalVille, finalSpecialite, PageRequest.of(0, 5)
        ).getContent();

        List<DoctorInfo> infos = medecins.stream().map(m -> new DoctorInfo(
                m.getId(), m.getNom(), m.getPrenom(),
                m.getSpecialite() != null ? m.getSpecialite().getNom() : null,
                m.getVille(), m.getAdresseCabinet()
        )).collect(Collectors.toList());

        return new DoctorSearchResponse(infos);
    }

    @Tool(description = "Recherche des laboratoires d'analyse par ville. La ville est totalement optionnelle. IMPORTANT: NE PASSE JAMAIS null. Si tu n'as pas de ville, passe une chaîne vide \"\".")
    public LaboSearchResponse searchNearbyLabos(@Nullable String ville) {
        String finalVille = (ville != null && ville.trim().isEmpty()) ? null : ville;

        List<Laboratoire> labos = laboRepository.searchApproved(
                null, finalVille, PageRequest.of(0, 5)
        ).getContent();

        List<LaboInfo> infos = labos.stream().map(l -> new LaboInfo(
                l.getId(), l.getNomLabo(), l.getVille(), l.getAdresseComplete()
        )).collect(Collectors.toList());

        return new LaboSearchResponse(infos);
    }

    @Tool(description = "Réserver un rendez-vous médical avec un médecin ou un laboratoire. " +
            "IMPORTANT: Tu ne dois JAMAIS deviner ou inventer la date ou l'heure. Si le patient ne les a pas données, demande-les avant d'appeler cet outil. " +
            "L'argument 'date' DOIT IMPÉRATIVEMENT être au format ISO yyyy-MM-dd (exemple: 2026-06-05). Ne passe jamais de date en lettres comme '6 juin'. " +
            "L'heure DOIT être au format HH:mm (ex: 14:00). " +
            "Pour medecinId et laboratoireId, si l'un d'eux n'est pas utilisé, passe -1.")
    public BookingResponse bookAppointment(String patientId, @Nullable String medecinId, @Nullable String laboratoireId, @Nullable String date, @Nullable String time, @Nullable String analyses) {
        System.out.println("[AI BOOKING] patientId=" + patientId + " medecinId=" + medecinId
                + " laboratoireId=" + laboratoireId + " date='" + date + "' time='" + time + "'");
        
        if (date == null || date.isBlank() || time == null || time.isBlank() || date.equals("null") || time.equals("null")) {
            return new BookingResponse(false, "ACTION REQUISE: Ne génère pas de rendez-vous ! Demande d'abord la date et l'heure au patient.");
        }

        try {
            Long parsedPatientId = Long.parseLong(patientId);
            Long parsedMedecinId = (medecinId != null && !medecinId.equals("-1") && !medecinId.isBlank() && !medecinId.equals("null")) ? Long.parseLong(medecinId) : null;
            Long parsedLaboratoireId = (laboratoireId != null && !laboratoireId.equals("-1") && !laboratoireId.isBlank() && !laboratoireId.equals("null")) ? Long.parseLong(laboratoireId) : null;

            Patient patient = patientRepository.findById(parsedPatientId)
                    .orElseThrow(() -> new RuntimeException("Patient non trouvé id=" + parsedPatientId));

            LocalDate parsedDate = parseFlexibleDate(date);
            LocalTime parsedTime = parseFlexibleTime(time);
            System.out.println("[AI BOOKING] Parsed -> date=" + parsedDate + " time=" + parsedTime);

            Appointment appointment = new Appointment();
            appointment.setPatient(patient);
            appointment.setDate(parsedDate);
            appointment.setTime(parsedTime);
            appointment.setStatus("CONFIRMED");

            if (parsedMedecinId != null && parsedMedecinId > 0) {
                Medecin medecin = medecinRepository.findById(parsedMedecinId)
                        .orElseThrow(() -> new RuntimeException("Médecin non trouvé id=" + parsedMedecinId));
                
                // Vérifier la disponibilité via AppointmentService
                boolean isAvailable = appointmentService.getAvailableTimeSlots(parsedMedecinId, parsedDate).stream()
                        .anyMatch(slot -> slot.isAvailable() && slot.getTime().equals(time));
                
                if (!isAvailable) {
                    return new BookingResponse(false, "ERREUR: Ce médecin ne travaille pas à cette heure-là ou le créneau est déjà pris. Demande au patient de choisir une autre heure et utilise l'outil checkDoctorAvailability pour voir les créneaux libres.");
                }

                appointment.setMedecin(medecin);
                System.out.println("[AI BOOKING] Médecin: " + medecin.getNom() + " " + medecin.getPrenom());
            } else if (parsedLaboratoireId != null && parsedLaboratoireId > 0) {
                Laboratoire labo = laboRepository.findById(parsedLaboratoireId)
                        .orElseThrow(() -> new RuntimeException("Laboratoire non trouvé id=" + parsedLaboratoireId));
                appointment.setLaboratoire(labo);
                if (analyses != null) appointment.setConsultationNotes(analyses);
                System.out.println("[AI BOOKING] Laboratoire: " + labo.getNomLabo());
            } else {
                return new BookingResponse(false, "Veuillez fournir un ID de médecin ou de laboratoire.");
            }

            Appointment saved = appointmentRepository.save(appointment);
            System.out.println("[AI BOOKING] SUCCESS -> appointmentId=" + saved.getId()
                    + " patientEntityId=" + saved.getPatient().getId());
            return new BookingResponse(true, "Rendez-vous réservé avec succès pour le " + parsedDate + " à " + parsedTime + ".");
        } catch (Exception e) {
            System.err.println("[AI BOOKING ERROR] " + e.getMessage());
            e.printStackTrace();
            return new BookingResponse(false, "Erreur lors de la réservation : " + e.getMessage());
        }
    }

    @Tool(description = "Vérifier les horaires disponibles d'un médecin pour une date donnée. IMPORTANT: L'argument 'date' DOIT IMPÉRATIVEMENT être au format ISO yyyy-MM-dd (exemple: 2026-06-05). Ne passe jamais de date en lettres comme '6 juin'. Utilise cet outil quand le patient demande si un médecin est disponible à une certaine date ou heure.")
    public String checkDoctorAvailability(String medecinId, String date) {
        try {
            Long parsedMedecinId = Long.parseLong(medecinId);
            LocalDate parsedDate = parseFlexibleDate(date);
            List<com.sehati.appointment.dto.TimeSlotDTO> slots = appointmentService.getAvailableTimeSlots(parsedMedecinId, parsedDate);
            
            String availableTimes = slots.stream()
                .filter(com.sehati.appointment.dto.TimeSlotDTO::isAvailable)
                .map(com.sehati.appointment.dto.TimeSlotDTO::getTime)
                .collect(Collectors.joining(", "));
                
            if (availableTimes.isEmpty()) {
                return "Aucun créneau disponible pour le " + date;
            }
            return "Créneaux disponibles le " + date + " : " + availableTimes;
        } catch (Exception e) {
            return "Erreur lors de la vérification des disponibilités : " + e.getMessage();
        }
    }

    @Tool(description = "Permet de lister tous les rendez-vous futurs du patient. Utilise cet outil quand le patient veut modifier ou annuler un rendez-vous pour connaître l'ID du rendez-vous (appointmentId).")
    public PatientAppointmentsResponse getPatientAppointments(String patientId) {
        System.out.println("[AI TOOL] getPatientAppointments called with patientId: " + patientId);
        try {
            Long pid = Long.parseLong(patientId);
            List<Appointment> apps = appointmentRepository.findByPatientId(pid).stream()
                    .filter(a -> a.getDate() != null && a.getTime() != null)
                    .filter(a -> a.getDate().isAfter(LocalDate.now()) || (a.getDate().isEqual(LocalDate.now()) && a.getTime().isAfter(LocalTime.now())))
                    .filter(a -> "CONFIRMED".equals(a.getStatus()))
                    .collect(Collectors.toList());
            
            System.out.println("[AI TOOL] Found " + apps.size() + " future confirmed appointments for patient " + pid);
            List<AppointmentInfo> infos = apps.stream().map(a -> new AppointmentInfo(
                    a.getId(), a.getDate().toString(), a.getTime().toString(),
                    a.getMedecin() != null ? "Dr. " + a.getMedecin().getNom() + " " + a.getMedecin().getPrenom() : (a.getLaboratoire() != null ? "Labo " + a.getLaboratoire().getNomLabo() : "Inconnu")
            )).collect(Collectors.toList());
            return new PatientAppointmentsResponse(infos);
        } catch (Exception e) {
            System.err.println("[AI TOOL ERROR] getPatientAppointments failed: " + e.getMessage());
            e.printStackTrace();
            return new PatientAppointmentsResponse(List.of());
        }
    }

    @Tool(description = "Permet d'annuler un rendez-vous existant. Nécessite l'ID du rendez-vous (appointmentId).")
    public BookingResponse cancelAppointment(String patientId, String appointmentId) {
        try {
            Long pId = Long.parseLong(patientId);
            Long aId = Long.parseLong(appointmentId);
            Appointment app = appointmentRepository.findById(aId)
                    .orElseThrow(() -> new RuntimeException("Rendez-vous introuvable."));
            if (!app.getPatient().getId().equals(pId)) {
                return new BookingResponse(false, "Non autorisé.");
            }
            app.setStatus("CANCELLED");
            appointmentRepository.save(app);
            return new BookingResponse(true, "Rendez-vous annulé avec succès.");
        } catch (Exception e) {
            return new BookingResponse(false, "Erreur d'annulation : " + e.getMessage());
        }
    }

    @Tool(description = "Permet de modifier (reporter) un rendez-vous existant à une nouvelle date et heure. IMPORTANT: La date DOIT IMPÉRATIVEMENT être au format ISO yyyy-MM-dd et l'heure HH:mm.")
    public BookingResponse modifyAppointment(String patientId, String appointmentId, String newDate, String newTime) {
        try {
            Long pId = Long.parseLong(patientId);
            Long aId = Long.parseLong(appointmentId);
            LocalDate parsedDate = parseFlexibleDate(newDate);
            LocalTime parsedTime = parseFlexibleTime(newTime);

            Appointment app = appointmentRepository.findById(aId)
                    .orElseThrow(() -> new RuntimeException("Rendez-vous introuvable."));
            if (!app.getPatient().getId().equals(pId)) {
                return new BookingResponse(false, "Non autorisé.");
            }

            if (app.getMedecin() != null) {
                boolean isAvailable = appointmentService.getAvailableTimeSlots(app.getMedecin().getId(), parsedDate).stream()
                        .anyMatch(slot -> slot.isAvailable() && slot.getTime().equals(newTime));
                if (!isAvailable) {
                    return new BookingResponse(false, "Le médecin n'est pas disponible à cette nouvelle date/heure. Demandez au patient de choisir un autre horaire.");
                }
            }

            app.setDate(parsedDate);
            app.setTime(parsedTime);
            appointmentRepository.save(app);
            return new BookingResponse(true, "Rendez-vous reporté avec succès pour le " + parsedDate + " à " + parsedTime + ".");
        } catch (Exception e) {
            return new BookingResponse(false, "Erreur de modification : " + e.getMessage());
        }
    }

    private LocalDate parseFlexibleDate(String date) {
        if (date == null || date.isBlank()) throw new RuntimeException("Date manquante");
        String d = date.trim();
        try { return LocalDate.parse(d, DateTimeFormatter.ISO_LOCAL_DATE); } catch (DateTimeParseException ignored) {}
        try { return LocalDate.parse(d, DateTimeFormatter.ofPattern("dd/MM/yyyy")); } catch (DateTimeParseException ignored) {}
        try { return LocalDate.parse(d, DateTimeFormatter.ofPattern("MM/dd/yyyy")); } catch (DateTimeParseException ignored) {}
        try { return LocalDate.parse(d, DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale.FRENCH)); } catch (DateTimeParseException ignored) {}
        try { return LocalDate.parse(d, DateTimeFormatter.ofPattern("dd-MM-yyyy")); } catch (DateTimeParseException ignored) {}
        throw new RuntimeException("Format de date non reconnu: '" + date + "'. Utilisez yyyy-MM-dd.");
    }

    private LocalTime parseFlexibleTime(String time) {
        if (time == null || time.isBlank()) throw new RuntimeException("Heure manquante");
        String t = time.trim().replace("h", ":").replace("H", ":");
        if (t.endsWith(":")) t = t.substring(0, t.length() - 1);
        try { return LocalTime.parse(t, DateTimeFormatter.ofPattern("HH:mm")); } catch (DateTimeParseException ignored) {}
        try { return LocalTime.parse(t, DateTimeFormatter.ofPattern("H:mm")); } catch (DateTimeParseException ignored) {}
        try { return LocalTime.parse(t, DateTimeFormatter.ISO_LOCAL_TIME); } catch (DateTimeParseException ignored) {}
        throw new RuntimeException("Format d'heure non reconnu: '" + time + "'. Utilisez HH:mm.");
    }

    // --- DTOs pour les Tools ---

    public record DoctorSearchResponse(List<DoctorInfo> doctors) {}
    public record DoctorInfo(Long id, String nom, String prenom, String specialite, String ville, String adresse) {}

    public record LaboSearchResponse(List<LaboInfo> laboratoires) {}
    public record LaboInfo(Long id, String nom, String ville, String adresse) {}

    public record BookingResponse(boolean success, String message) {}

    public record PatientAppointmentsResponse(List<AppointmentInfo> appointments) {}
    public record AppointmentInfo(Long appointmentId, String date, String time, String professionnel) {}
}
