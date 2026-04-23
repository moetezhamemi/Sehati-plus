package com.sehati.appointment.service;

import com.sehati.appointment.dto.AppointmentDetailDTO;
import com.sehati.appointment.dto.AppointmentRequestDTO;
import com.sehati.appointment.dto.AppointmentResponseDTO;
import com.sehati.appointment.dto.AvailableDateDTO;
import com.sehati.appointment.dto.ConsultationAppointmentDTO;
import com.sehati.appointment.dto.OrdonnanceRequestDTO;
import com.sehati.appointment.dto.DemandeAnalyseRequestDTO;
import com.sehati.appointment.dto.PatientAppointmentDTO;
import com.sehati.appointment.dto.ScheduleMetaDTO;
import com.sehati.appointment.dto.TimeSlotDTO;
import com.sehati.appointment.dto.BlockSlotRequestDTO;
import com.sehati.appointment.dto.BlockedSlotDTO;
import com.sehati.appointment.dto.ManualAppointmentRequestDTO;
import com.sehati.appointment.entities.Appointment;
import com.sehati.appointment.entities.BlockedSlot;
import com.sehati.appointment.repository.AppointmentRepository;
import com.sehati.appointment.repository.BlockedSlotRepository;
import com.sehati.common.entities.DaySchedule;
import com.sehati.common.entities.WorkHours;
import com.sehati.laboratoire.entities.Laboratoire;
import com.sehati.laboratoire.repository.LaboratoireRepository;
import com.sehati.medecin.entities.Medecin;
import com.sehati.medecin.repository.MedecinRepository;
import com.sehati.patient.entities.Patient;
import com.sehati.patient.repository.PatientRepository;
import com.sehati.secretaire.entities.Secretaire;
import com.sehati.secretaire.repository.SecretaireRepository;
import com.sehati.secretaire.repository.MedecinSecretaireRepository;
import com.sehati.secretaire.entities.MedecinSecretaire;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentService {
  
    private final AppointmentRepository appointmentRepository;
    private final BlockedSlotRepository blockedSlotRepository;
    private final MedecinRepository medecinRepository;
    private final LaboratoireRepository laboratoireRepository;
    private final PatientRepository patientRepository;
    private final SecretaireRepository secretaireRepository;
    private final MedecinSecretaireRepository medecinSecretaireRepository;
    private final com.sehati.patient.service.PatientService patientService;
    private final OrdonnancePdfService ordonnancePdfService;
    private final com.sehati.common.service.CloudinaryService cloudinaryService;

    private static final String[] DAYS_FR = { "Dim.", "Lun.", "Mar.", "Mer.", "Jeu.", "Ven.", "Sam." };
    private static final String[] MONTHS_FR = { "Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août",
            "Septembre", "Octobre", "Novembre", "Décembre" };

    public List<AvailableDateDTO> getAvailableDates(Long medecinId) {
        Medecin medecin = medecinRepository.findById(medecinId)
                .orElseThrow(() -> new RuntimeException("Medecin not found"));
        WorkHours wh = medecin.getWorkHours();

        List<AvailableDateDTO> dates = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 7; i++) {
            LocalDate date = today.plusDays(i);
            int dayOfWeekValue = date.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
            boolean disabled = false;

            if (wh != null) {
                DaySchedule ds = getDayScheduleByDayOfWeek(wh, date.getDayOfWeek());
                if (ds == null || ds.isFerme()) {
                    disabled = true;
                }
            }

            int indexFr = dayOfWeekValue == 7 ? 0 : dayOfWeekValue;

            dates.add(AvailableDateDTO.builder()
                    .dayName(DAYS_FR[indexFr])
                    .dayNumber(date.getDayOfMonth())
                    .month(MONTHS_FR[date.getMonthValue() - 1])
                    .fullDate(date.toString())
                    .disabled(disabled)
                    .build());
        }
        return dates;
    }

    public List<AvailableDateDTO> getAvailableLaboDates(Long laboId) {
        Laboratoire labo = laboratoireRepository.findById(laboId)
                .orElseThrow(() -> new RuntimeException("Laboratoire not found"));
        WorkHours wh = labo.getWorkHours();

        List<AvailableDateDTO> dates = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 7; i++) {
            LocalDate date = today.plusDays(i);
            int dayOfWeekValue = date.getDayOfWeek().getValue();
            boolean disabled = false;

            if (wh != null) {
                DaySchedule ds = getDayScheduleByDayOfWeek(wh, date.getDayOfWeek());
                if (ds == null || ds.isFerme()) {
                    disabled = true;
                }
            }

            int indexFr = dayOfWeekValue == 7 ? 0 : dayOfWeekValue;

            dates.add(AvailableDateDTO.builder()
                    .dayName(DAYS_FR[indexFr])
                    .dayNumber(date.getDayOfMonth())
                    .month(MONTHS_FR[date.getMonthValue() - 1])
                    .fullDate(date.toString())
                    .disabled(disabled)
                    .build());
        }
        return dates;
    }

    public List<TimeSlotDTO> getAvailableTimeSlots(Long medecinId, LocalDate date) {
        Medecin medecin = medecinRepository.findById(medecinId).orElseThrow();
        WorkHours wh = medecin.getWorkHours();

        int consultationTime = medecin.getConsultationTime() != null ? medecin.getConsultationTime() : 30;

        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(17, 0);

        if (wh != null) {
            DaySchedule ds = getDayScheduleByDayOfWeek(wh, date.getDayOfWeek());
            if (ds != null && !ds.isFerme()) {
                start = ds.getDebut() != null ? ds.getDebut() : start;
                end = ds.getFin() != null ? ds.getFin() : end;
            } else if (ds != null && ds.isFerme()) {
                return new ArrayList<>(); // closed
            }
        }

        List<Appointment> bookedAppointments = appointmentRepository.findByMedecinIdAndDateAndStatus(medecinId, date,
                "CONFIRMED");
        List<LocalTime> bookedTimes = bookedAppointments.stream().map(Appointment::getTime)
                .collect(Collectors.toList());

        List<BlockedSlot> blockedSlots = blockedSlotRepository.findByOwnerIdAndOwnerTypeAndDate(medecinId, "MEDECIN",
                date);

        List<TimeSlotDTO> slots = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalTime current = start;
        while (!current.plusMinutes(consultationTime).isAfter(end)) {
            final LocalTime slotTime = current;
            boolean isBookedByAppointment = bookedTimes.contains(slotTime);
            boolean isBlocked = blockedSlots.stream()
                    .anyMatch(b -> (slotTime.equals(b.getStartTime()) || slotTime.isAfter(b.getStartTime()))
                            && slotTime.isBefore(b.getEndTime()));

            slots.add(TimeSlotDTO.builder()
                    .time(current.format(formatter))
                    .available(!isBookedByAppointment && !isBlocked)
                    .build());
            current = current.plusMinutes(consultationTime);
        }

        return slots;
    }

    public List<TimeSlotDTO> getAvailableLaboTimeSlots(Long laboId, LocalDate date) {
        Laboratoire labo = laboratoireRepository.findById(laboId).orElseThrow();
        WorkHours wh = labo.getWorkHours();

        int consultationTime = labo.getConsultationTime() != null ? labo.getConsultationTime() : 15;

        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(18, 0);

        if (wh != null) {
            DaySchedule ds = getDayScheduleByDayOfWeek(wh, date.getDayOfWeek());
            if (ds != null && !ds.isFerme()) {
                start = ds.getDebut() != null ? ds.getDebut() : start;
                end = ds.getFin() != null ? ds.getFin() : end;
            } else if (ds != null && ds.isFerme()) {
                return new ArrayList<>(); // closed
            }
        }

        int capacite = labo.getCapaciteParCreneau() != null ? labo.getCapaciteParCreneau() : 1;

        List<Appointment> bookedAppointments = appointmentRepository.findByLaboratoireIdAndDateAndStatus(laboId, date,
                "CONFIRMED");
        java.util.Map<LocalTime, Long> timeSlotCounts = bookedAppointments.stream()
                .collect(Collectors.groupingBy(Appointment::getTime, Collectors.counting()));

        List<BlockedSlot> blockedSlots = blockedSlotRepository.findByOwnerIdAndOwnerTypeAndDate(laboId, "LABORATOIRE",
                date);

        List<TimeSlotDTO> slots = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalTime current = start;
        while (!current.plusMinutes(consultationTime).isAfter(end)) {
            final LocalTime slotTime = current;
            long bookingsAtCurrent = timeSlotCounts.getOrDefault(slotTime, 0L);
            boolean isBookedByAppointment = bookingsAtCurrent >= capacite;
            boolean isBlocked = blockedSlots.stream()
                    .anyMatch(b -> (slotTime.equals(b.getStartTime()) || slotTime.isAfter(b.getStartTime()))
                            && slotTime.isBefore(b.getEndTime()));

            slots.add(TimeSlotDTO.builder()
                    .time(current.format(formatter))
                    .available(!isBookedByAppointment && !isBlocked)
                    .build());
            current = current.plusMinutes(consultationTime);
        }

        return slots;
    }

    @Transactional
    public void blockSlots(Long userId, BlockSlotRequestDTO request, String role) {
        Long ownerId = userId;
        String ownerType = role;

        if ("SECRETAIRE".equals(role)) {
            Secretaire secretaire = secretaireRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Secretaire not found"));
            MedecinSecretaire relation = medecinSecretaireRepository
                    .findBySecretaireUserIdAndStatus(secretaire.getUser().getId(), "ACTIVE")
                    .orElseThrow(() -> new RuntimeException("Aucune relation active pour cette secrétaire"));
            ownerId = relation.getMedecin().getId();
            ownerType = "MEDECIN";
        } else if ("MEDECIN".equals(role)) {
            Medecin medecin = medecinRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Medecin not found"));
            ownerId = medecin.getId();
        } else if ("LABORATOIRE".equals(role)) {
            Laboratoire labo = laboratoireRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Laboratoire not found"));
            ownerId = labo.getId();
        }

        BlockedSlot blockedSlot = BlockedSlot.builder()
                .ownerId(ownerId)
                .ownerType(ownerType)
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();
        blockedSlotRepository.save(blockedSlot);

        // Cancel existing appointments in this time range
        List<Appointment> existingAppointments;
        if ("MEDECIN".equals(ownerType)) {
            existingAppointments = appointmentRepository.findByMedecinIdAndDateOrderByTimeAsc(ownerId,
                    request.getDate());
        } else {
            existingAppointments = appointmentRepository.findByLaboratoireIdAndDateOrderByTimeAsc(ownerId,
                    request.getDate());
        }

        for (Appointment app : existingAppointments) {
            if ("CONFIRMED".equals(app.getStatus())) {
                LocalTime t = app.getTime();
                if ((t.equals(request.getStartTime()) || t.isAfter(request.getStartTime()))
                        && t.isBefore(request.getEndTime())) {
                    app.setStatus("CANCELLED");
                    appointmentRepository.save(app);
                }
            }
        }
    }

    @Transactional
    public void unblockSlot(Long blockedSlotId, Long userId, String role) {
        BlockedSlot blockedSlot = blockedSlotRepository.findById(blockedSlotId)
                .orElseThrow(() -> new RuntimeException("Blocked slot not found"));

        Long ownerId = userId;
        String ownerType = role;

        if ("SECRETAIRE".equals(role)) {
            Secretaire secretaire = secretaireRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Secretaire not found"));
            MedecinSecretaire relation = medecinSecretaireRepository
                    .findBySecretaireUserIdAndStatus(secretaire.getUser().getId(), "ACTIVE")
                    .orElseThrow(() -> new RuntimeException("Aucune relation active pour cette secrétaire"));
            ownerId = relation.getMedecin().getId();
            ownerType = "MEDECIN";
        } else if ("MEDECIN".equals(role)) {
            Medecin medecin = medecinRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Medecin not found"));
            ownerId = medecin.getId();
        } else if ("LABORATOIRE".equals(role)) {
            Laboratoire labo = laboratoireRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Laboratoire not found"));
            ownerId = labo.getId();
        }

        if (!blockedSlot.getOwnerId().equals(ownerId) || !blockedSlot.getOwnerType().equals(ownerType)) {
            throw new RuntimeException("You are not authorized to unblock this slot");
        }

        blockedSlotRepository.delete(blockedSlot);
    }

    @Transactional
    public AppointmentResponseDTO createAppointment(Long userId, AppointmentRequestDTO request) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Patient not found for user ID: " + userId));

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDate(request.getDate());
        appointment.setTime(request.getTime());
        appointment.setStatus("CONFIRMED"); // Auto-confirmed logic
        appointment.setAnalysesNames(request.getAnalysesNames());
        appointment.setOrdonnanceUrl(request.getOrdonnanceUrl());

        if (request.getMedecinId() != null) {
            Medecin medecin = medecinRepository.findById(request.getMedecinId())
                    .orElseThrow(() -> new RuntimeException("Medecin not found"));

            List<Appointment> existing = appointmentRepository.findByMedecinIdAndDateAndStatus(
                    medecin.getId(), request.getDate(), "CONFIRMED");

            if (existing.stream().anyMatch(app -> app.getTime().equals(request.getTime()))) {
                throw new RuntimeException("Time slot is already booked");
            }

            List<BlockedSlot> blockedSlots = blockedSlotRepository.findByOwnerIdAndOwnerTypeAndDate(medecin.getId(),
                    "MEDECIN", request.getDate());
            if (blockedSlots.stream().anyMatch(
                    b -> (request.getTime().equals(b.getStartTime()) || request.getTime().isAfter(b.getStartTime()))
                            && request.getTime().isBefore(b.getEndTime()))) {
                throw new RuntimeException("Time slot is blocked");
            }
            appointment.setMedecin(medecin);
        } else if (request.getLaboratoireId() != null) {
            Laboratoire labo = laboratoireRepository.findById(request.getLaboratoireId())
                    .orElseThrow(() -> new RuntimeException("Laboratoire not found"));

            List<Appointment> existing = appointmentRepository.findByLaboratoireIdAndDateAndStatus(
                    labo.getId(), request.getDate(), "CONFIRMED");

            int capacite = labo.getCapaciteParCreneau() != null ? labo.getCapaciteParCreneau() : 1;
            long bookingsAtSlot = existing.stream().filter(app -> app.getTime().equals(request.getTime())).count();

            if (bookingsAtSlot >= capacite) {
                throw new RuntimeException("Time slot is already booked for maximum capacity");
            }

            List<BlockedSlot> blockedSlots = blockedSlotRepository.findByOwnerIdAndOwnerTypeAndDate(labo.getId(),
                    "LABORATOIRE", request.getDate());
            if (blockedSlots.stream().anyMatch(
                    b -> (request.getTime().equals(b.getStartTime()) || request.getTime().isAfter(b.getStartTime()))
                            && request.getTime().isBefore(b.getEndTime()))) {
                throw new RuntimeException("Time slot is blocked");
            }

            appointment.setLaboratoire(labo);
        } else {
            throw new RuntimeException("Neither medecinId nor laboratoireId provided");
        }

        appointment = appointmentRepository.save(appointment);

        return AppointmentResponseDTO.builder()
                .id(appointment.getId())
                .medecinId(appointment.getMedecin() != null ? appointment.getMedecin().getId() : null)
                .laboratoireId(appointment.getLaboratoire() != null ? appointment.getLaboratoire().getId() : null)
                .patientId(patient.getId())
                .analysesNames(appointment.getAnalysesNames())
                .ordonnanceUrl(appointment.getOrdonnanceUrl())

                .date(appointment.getDate())
                .time(appointment.getTime())

                .status(appointment.getStatus())
                .build();
    }

    public List<PatientAppointmentDTO> getMyUpcomingAppointments(Long userId) {
        Patient patient = patientRepository.findByUserId(userId)

                .orElseThrow(() -> new RuntimeException("Patient not found for user ID: " + userId));

        LocalDate today = LocalDate.now();
        // Fetch appointments with date strictly after yesterday (i.e. today and future)
        List<Appointment> appointments = appointmentRepository
                .findByPatientIdAndDateAfterOrderByDateAscTimeAsc(patient.getId(), today.minusDays(1));

        return appointments.stream()
                .filter(a -> !a.getDate().isBefore(today)) // only today or future
                .map(a -> {
                    String providerName;
                    String type;
                    String specialite = null;
                    String address;
                    Long providerId;

                    if (a.getMedecin() != null) {
                        providerName = "Dr " + a.getMedecin().getPrenom() + " " + a.getMedecin().getNom();
                        type = "Consultation";
                        specialite = a.getMedecin().getSpecialite();
                        address = a.getMedecin().getAdresseCabinet() != null
                                ? a.getMedecin().getAdresseCabinet()
                                : a.getMedecin().getVille();
                        providerId = a.getMedecin().getId();
                    } else {
                        providerName = a.getLaboratoire().getNomLabo();
                        type = "Analyse";
                        address = a.getLaboratoire().getAdresseComplete() != null
                                ? a.getLaboratoire().getAdresseComplete()
                                : a.getLaboratoire().getVille();
                        providerId = a.getLaboratoire().getId();
                    }

                    return PatientAppointmentDTO.builder()
                            .id(a.getId())
                            .providerId(providerId)
                            .providerName(providerName)
                            .type(type)
                            .specialite(specialite)
                            .dateTime(a.getDate().atTime(a.getTime()))
                            .address(address)
                            .status(a.getStatus())
                            .resultUrl(a.getResultUrl())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public Page<PatientAppointmentDTO> getMyPastAppointments(Long userId, Pageable pageable) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Patient not found for user ID: " + userId));

        LocalDate today = LocalDate.now();
        Page<Appointment> appointments = appointmentRepository
                .findByPatientIdAndStatusAndDateBeforeOrderByDateDescTimeDesc(patient.getId(), "COMPLETED", today,
                        pageable);

        List<PatientAppointmentDTO> dtoList = appointments.stream()
                .map(a -> {
                    String providerName;
                    String type;
                    String specialite = null;
                    String address;
                    String providerPdpUrl = null;
                    Long providerId;

                    if (a.getMedecin() != null) {
                        providerName = "Dr " + a.getMedecin().getPrenom() + " " + a.getMedecin().getNom();
                        type = "Médecin";
                        specialite = a.getMedecin().getSpecialite();
                        address = a.getMedecin().getAdresseCabinet() != null
                                ? a.getMedecin().getAdresseCabinet()
                                : a.getMedecin().getVille();
                        providerPdpUrl = a.getMedecin().getPhotoProfilUrl();
                        providerId = a.getMedecin().getId();
                    } else {
                        providerName = a.getLaboratoire().getNomLabo();
                        type = "Laboratoire";
                        address = a.getLaboratoire().getAdresseComplete() != null
                                ? a.getLaboratoire().getAdresseComplete()
                                : a.getLaboratoire().getVille();
                        providerPdpUrl = a.getLaboratoire().getPhotoProfilUrl();
                        providerId = a.getLaboratoire().getId();
                    }

                    return PatientAppointmentDTO.builder()
                            .id(a.getId())
                            .providerId(providerId)
                            .providerName(providerName)
                            .type(type)
                            .specialite(specialite)
                            .dateTime(a.getDate().atTime(a.getTime()))
                            .address(address)
                            .status(a.getStatus())
                            .resultUrl(a.getResultUrl())
                            .providerPdpUrl(providerPdpUrl)
                            .ordonnanceUrl(a.getOrdonnanceUrl())
                            .demandeAnalyseUrl(a.getDemandeAnalyseUrl())
                            .build();
                })
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, appointments.getTotalElements());
    }

    @Transactional
    public void cancelAppointment(Long userId, Long appointmentId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Patient not found for user ID: " + userId));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found with ID: " + appointmentId));

        // Security check: ensure the appointment belongs to the patient
        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("Unauthorized: You do not own this appointment");
        }

        // Only allow cancelling if it's not already cancelled
        if ("CANCELLED".equals(appointment.getStatus())) {
            throw new RuntimeException("Appointment is already cancelled");
        }

        appointment.setStatus("CANCELLED");
        appointmentRepository.save(appointment);
    }

    @Transactional
    public void providerCancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Rendez-vous introuvable : " + appointmentId));

        if ("CANCELLED".equals(appointment.getStatus())) {
            throw new RuntimeException("Ce rendez-vous est déjà annulé");
        }

        appointment.setStatus("CANCELLED");
        appointmentRepository.save(appointment);
    }

    @Transactional
    public void completeAppointment(Long appointmentId, String notes) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Rendez-vous introuvable : " + appointmentId));

        if ("COMPLETED".equals(appointment.getStatus())) {
            throw new RuntimeException("Ce rendez-vous est déjà terminé");
        }
        if ("CANCELLED".equals(appointment.getStatus())) {
            throw new RuntimeException("Impossible de terminer un rendez-vous annulé");
        }

        if (notes != null && !notes.trim().isEmpty()) {
            appointment.setConsultationNotes(notes);
        }

        appointment.setStatus("COMPLETED");
        appointmentRepository.save(appointment);
    }

    @Transactional
    public void updateResultUrl(Long appointmentId, String resultUrl) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Rendez-vous introuvable : " + appointmentId));
        appointment.setResultUrl(resultUrl);
        appointment.setStatus("COMPLETED");
        appointmentRepository.save(appointment);
    }

    @Transactional
    public String generateOrdonnance(Long appointmentId, OrdonnanceRequestDTO requestDTO) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Rendez-vous introuvable"));

        String pdfUrl = ordonnancePdfService.generateAndUploadOrdonnance(appointment, requestDTO);

        appointment.setOrdonnanceUrl(pdfUrl);
        appointmentRepository.save(appointment);

        return pdfUrl;
    }

    @Transactional
    public void deleteOrdonnance(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Rendez-vous introuvable"));

        if (appointment.getOrdonnanceUrl() != null) {
            cloudinaryService.deleteFile(appointment.getOrdonnanceUrl());
            appointment.setOrdonnanceUrl(null);
            appointmentRepository.save(appointment);
        }
    }

    @Transactional
    public String generateDemandeAnalyse(Long appointmentId, DemandeAnalyseRequestDTO requestDTO) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Rendez-vous introuvable"));

        String pdfUrl = ordonnancePdfService.generateAndUploadDemandeAnalyse(appointment, requestDTO);

        appointment.setDemandeAnalyseUrl(pdfUrl);
        appointmentRepository.save(appointment);

        return pdfUrl;
    }

    @Transactional
    public void deleteDemandeAnalyse(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Rendez-vous introuvable"));

        if (appointment.getDemandeAnalyseUrl() != null) {
            cloudinaryService.deleteFile(appointment.getDemandeAnalyseUrl());
            appointment.setDemandeAnalyseUrl(null);
            appointmentRepository.save(appointment);
        }
    }

    private DaySchedule getDayScheduleByDayOfWeek(WorkHours wh, DayOfWeek dow) {
        return switch (dow) {
            case MONDAY -> wh.getLundi();
            case TUESDAY -> wh.getMardi();
            case WEDNESDAY -> wh.getMercredi();
            case THURSDAY -> wh.getJeudi();
            case FRIDAY -> wh.getVendredi();
            case SATURDAY -> wh.getSamedi();
            case SUNDAY -> wh.getDimanche();
        };
    }

    // =========================================================
    // Consultation des rendez-vous : Médecin
    // =========================================================

    public List<ConsultationAppointmentDTO> getAppointmentsForMedecin(Long userId, LocalDate date) {
        Medecin medecin = medecinRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Médecin introuvable pour l'utilisateur : " + userId));
        return appointmentRepository
                .findByMedecinIdAndDateOrderByTimeAsc(medecin.getId(), date)
                .stream()
                .filter(a -> "CONFIRMED".equals(a.getStatus()))
                .map(this::toConsultationDTO)
                .collect(Collectors.toList());
    }

    public List<ConsultationAppointmentDTO> getWeeklyAppointmentsForMedecin(
            Long userId, LocalDate weekStart, LocalDate weekEnd) {
        Medecin medecin = medecinRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Médecin introuvable pour l'utilisateur : " + userId));
        return appointmentRepository
                .findByMedecinIdAndDateBetweenOrderByDateAscTimeAsc(medecin.getId(), weekStart, weekEnd)
                .stream()
                .filter(a -> "CONFIRMED".equals(a.getStatus()))
                .map(this::toConsultationDTO)
                .collect(Collectors.toList());
    }

    public ScheduleMetaDTO getScheduleMetaForMedecin(Long userId) {
        Medecin medecin = medecinRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Médecin introuvable pour l'utilisateur : " + userId));
        int slotDuration = medecin.getConsultationTime() != null ? medecin.getConsultationTime() : 30;
        return buildScheduleMeta(medecin.getWorkHours(), slotDuration);
    }

    // =========================================================
    // Consultation des rendez-vous : Secrétaire
    // =========================================================

    public List<ConsultationAppointmentDTO> getAppointmentsForSecretaire(Long userId, LocalDate date) {
        Secretaire secretaire = secretaireRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Secrétaire introuvable pour l'utilisateur : " + userId));
        Medecin medecin = medecinSecretaireRepository
                .findBySecretaireUserIdAndStatus(secretaire.getUser().getId(), "ACTIVE")
                .orElseThrow(() -> new RuntimeException("Aucune relation active pour cette secrétaire")).getMedecin();
        return appointmentRepository
                .findByMedecinIdAndDateOrderByTimeAsc(medecin.getId(), date)
                .stream()
                .map(this::toConsultationDTO)
                .collect(Collectors.toList());
    }

    public List<ConsultationAppointmentDTO> getWeeklyAppointmentsForSecretaire(
            Long userId, LocalDate weekStart, LocalDate weekEnd) {
        Secretaire secretaire = secretaireRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Secrétaire introuvable pour l'utilisateur : " + userId));
        Medecin medecin = medecinSecretaireRepository
                .findBySecretaireUserIdAndStatus(secretaire.getUser().getId(), "ACTIVE")
                .orElseThrow(() -> new RuntimeException("Aucune relation active pour cette secrétaire")).getMedecin();
        return appointmentRepository
                .findByMedecinIdAndDateBetweenOrderByDateAscTimeAsc(medecin.getId(), weekStart, weekEnd)
                .stream()
                .map(this::toConsultationDTO)
                .collect(Collectors.toList());
    }

    public List<com.sehati.appointment.dto.BlockedSlotDTO> getWeeklyBlockedSlots(Long userId, String role,
            LocalDate weekStart, LocalDate weekEnd) {
        Long ownerId = userId;
        String ownerType = role;

        if ("SECRETAIRE".equals(role)) {
            Secretaire secretaire = secretaireRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Secretaire not found"));
            MedecinSecretaire relation = medecinSecretaireRepository
                    .findBySecretaireUserIdAndStatus(secretaire.getUser().getId(), "ACTIVE")
                    .orElseThrow(() -> new RuntimeException("Aucune relation active pour cette secrétaire"));
            ownerId = relation.getMedecin().getId();
            ownerType = "MEDECIN";
        } else if ("MEDECIN".equals(role)) {
            Medecin medecin = medecinRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Medecin not found"));
            ownerId = medecin.getId();
        } else if ("LABORATOIRE".equals(role)) {
            Laboratoire labo = laboratoireRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Laboratoire not found"));
            ownerId = labo.getId();
        }

        return blockedSlotRepository.findByOwnerIdAndOwnerTypeAndDateBetween(ownerId, ownerType, weekStart, weekEnd)
                .stream()
                .map(b -> com.sehati.appointment.dto.BlockedSlotDTO.builder()
                        .id(b.getId())
                        .date(b.getDate())
                        .startTime(b.getStartTime())
                        .endTime(b.getEndTime())
                        .type("BLOCKED")
                        .build())
                .collect(Collectors.toList());
    }

    public ScheduleMetaDTO getScheduleMetaForSecretaire(Long userId) {
        Secretaire secretaire = secretaireRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Secrétaire introuvable pour l'utilisateur : " + userId));
        Medecin medecin = medecinSecretaireRepository
                .findBySecretaireUserIdAndStatus(secretaire.getUser().getId(), "ACTIVE")
                .orElseThrow(() -> new RuntimeException("Aucune relation active pour cette secrétaire")).getMedecin();
        int slotDuration = medecin.getConsultationTime() != null ? medecin.getConsultationTime() : 30;
        return buildScheduleMeta(medecin.getWorkHours(), slotDuration);
    }

    // =========================================================
    // Consultation des rendez-vous : Laboratoire
    // =========================================================

    public List<ConsultationAppointmentDTO> getAppointmentsForLabo(Long userId, LocalDate date) {
        Laboratoire labo = laboratoireRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Laboratoire introuvable pour l'utilisateur : " + userId));
        return appointmentRepository
                .findByLaboratoireIdAndDateOrderByTimeAsc(labo.getId(), date)
                .stream()
                .filter(a -> "CONFIRMED".equals(a.getStatus()))
                .map(this::toConsultationDTO)
                .collect(Collectors.toList());
    }

    public List<ConsultationAppointmentDTO> getWeeklyAppointmentsForLabo(
            Long userId, LocalDate weekStart, LocalDate weekEnd) {
        Laboratoire labo = laboratoireRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Laboratoire introuvable pour l'utilisateur : " + userId));
        return appointmentRepository
                .findByLaboratoireIdAndDateBetweenOrderByDateAscTimeAsc(labo.getId(), weekStart, weekEnd)
                .stream()
                .filter(a -> "CONFIRMED".equals(a.getStatus()))
                .map(this::toConsultationDTO)
                .collect(Collectors.toList());
    }

    public ScheduleMetaDTO getScheduleMetaForLabo(Long userId) {
        Laboratoire labo = laboratoireRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Laboratoire introuvable pour l'utilisateur : " + userId));
        int slotDuration = labo.getConsultationTime() != null ? labo.getConsultationTime() : 15;
        return buildScheduleMeta(labo.getWorkHours(), slotDuration);
    }

    // =========================================================
    // Détails d'un rendez-vous pour le professionnel
    // =========================================================
    public AppointmentDetailDTO getAppointmentDetails(Long appointmentId, Long userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Rendez-vous introuvable : " + appointmentId));

        // Note: For now, we return it. If we want stricter security, we'd verify that
        // the appointment
        // is linked to the medecin or laboratoire corresponding to userId. But since we
        // check roles in the controller, it's partially secured.

        Patient patient = appointment.getPatient();

        // Check if the patient has at least one COMPLETED appointment with this medecin
        boolean hasHistory = false;
        if (patient != null && appointment.getMedecin() != null) {
            hasHistory = !appointmentRepository
                    .findByPatientIdAndMedecinIdAndStatusAndDeletedByMedecinFalseOrderByDateDescTimeDesc(
                            patient.getId(), appointment.getMedecin().getId(), "COMPLETED")
                    .isEmpty();
        }

        return com.sehati.appointment.dto.AppointmentDetailDTO.builder()
                .id(appointment.getId())
                .patientId(patient != null ? patient.getId() : null)
                .patientNom(patient != null ? patient.getNom() : "")
                .patientPrenom(patient != null ? patient.getPrenom() : "")
                .patientPdpUrl(patient != null ? patient.getPhotoProfilUrl() : null)
                .patientTelephone(patient != null ? patient.getTelephone() : "")
                .date(appointment.getDate())
                .time(appointment.getTime())
                .status(appointment.getStatus())
                .consultationNotes(appointment.getConsultationNotes())
                .analysesNames(appointment.getAnalysesNames())
                .ordonnanceUrl(appointment.getOrdonnanceUrl())
                .resultUrl(appointment.getResultUrl())
                .hasHistory(hasHistory)
                .build();
    }

    // =========================================================
    // Méthodes utilitaires privées
    // =========================================================

    private ConsultationAppointmentDTO toConsultationDTO(Appointment appointment) {
        Patient patient = appointment.getPatient();
        return ConsultationAppointmentDTO.builder()
                .id(appointment.getId())
                .patientNom(patient != null ? patient.getNom() : "")
                .patientPrenom(patient != null ? patient.getPrenom() : "")
                .patientTelephone(patient != null ? patient.getTelephone() : "")
                .date(appointment.getDate())
                .time(appointment.getTime())
                .status(appointment.getStatus())
                .analysesNames(appointment.getAnalysesNames())
                .build();
    }

    /**
     * Construit les métadonnées de l'emploi du temps à partir des WorkHours du
     * professionnel.
     * - step = consultation_time du professionnel
     * - debut = heure de début la plus tôt parmi les jours travaillés
     * - fin = heure de fin la plus tardive parmi les jours travaillés
     * - workingDays = jours non fermés
     */
    private ScheduleMetaDTO buildScheduleMeta(WorkHours wh, int slotDuration) {
        List<DayOfWeek> allDays = Arrays.asList(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

        List<String> workingDays = new ArrayList<>();
        LocalTime globalStart = LocalTime.of(9, 0);
        LocalTime globalEnd = LocalTime.of(17, 0);
        boolean firstWorkingDay = true;

        if (wh != null) {
            for (DayOfWeek dow : allDays) {
                DaySchedule ds = getDayScheduleByDayOfWeek(wh, dow);
                if (ds != null && !ds.isFerme() && ds.getDebut() != null && ds.getFin() != null) {
                    workingDays.add(dow.name());
                    if (firstWorkingDay) {
                        globalStart = ds.getDebut();
                        globalEnd = ds.getFin();
                        firstWorkingDay = false;
                    } else {
                        if (ds.getDebut().isBefore(globalStart))
                            globalStart = ds.getDebut();
                        if (ds.getFin().isAfter(globalEnd))
                            globalEnd = ds.getFin();
                    }
                }
            }
        } else {
            // Pas de WorkHours définis : semaine standard du lundi au vendredi
            workingDays = Arrays.asList(
                    "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY");
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        return ScheduleMetaDTO.builder()
                .slotDurationMinutes(slotDuration)
                .earliestStart(globalStart.format(fmt))
                .latestEnd(globalEnd.format(fmt))
                .workingDays(workingDays)
                .build();
    }

    // =========================================================
    // Création manuelle de RDV par médecin ou secrétaire
    // =========================================================

    @Transactional
    public AppointmentResponseDTO createManualAppointment(Long userId, String role, ManualAppointmentRequestDTO request) {
        // Resolve medecinId based on role
        Long medecinId;
        if ("MEDECIN".equals(role)) {
            Medecin medecin = medecinRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Médecin introuvable"));
            medecinId = medecin.getId();
        } else if ("SECRETAIRE".equals(role)) {
            Secretaire secretaire = secretaireRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Secrétaire introuvable"));
            MedecinSecretaire relation = medecinSecretaireRepository
                    .findBySecretaireUserIdAndStatus(secretaire.getUser().getId(), "ACTIVE")
                    .orElseThrow(() -> new RuntimeException("Aucune relation active pour cette secrétaire"));
            medecinId = relation.getMedecin().getId();
        } else {
            throw new RuntimeException("Rôle non autorisé pour la création manuelle de RDV");
        }

        Medecin medecin = medecinRepository.findById(medecinId)
                .orElseThrow(() -> new RuntimeException("Médecin introuvable"));

        Patient patient;
        if (request.getPatientId() != null) {
            patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient introuvable"));
        } else if (request.getNewPatient() != null) {
            // Création du patient à la volée
            com.sehati.patient.dto.ProfessionalPatientDTO patientDTO = patientService.createPatientManually(request.getNewPatient());
            patient = patientRepository.findById(patientDTO.getId())
                    .orElseThrow(() -> new RuntimeException("Erreur lors de la création du patient"));
        } else {
            throw new RuntimeException("Les informations du patient sont obligatoires");
        }

        // Check slot availability
        List<Appointment> existing = appointmentRepository.findByMedecinIdAndDateAndStatus(
                medecinId, request.getDate(), "CONFIRMED");
        if (existing.stream().anyMatch(app -> app.getTime().equals(request.getTime()))) {
            throw new RuntimeException("Ce créneau est déjà pris");
        }

        List<BlockedSlot> blockedSlots = blockedSlotRepository.findByOwnerIdAndOwnerTypeAndDate(
                medecinId, "MEDECIN", request.getDate());
        if (blockedSlots.stream().anyMatch(
                b -> (request.getTime().equals(b.getStartTime()) || request.getTime().isAfter(b.getStartTime()))
                        && request.getTime().isBefore(b.getEndTime()))) {
            throw new RuntimeException("Ce créneau est bloqué");
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setMedecin(medecin);
        appointment.setDate(request.getDate());
        appointment.setTime(request.getTime());
        appointment.setStatus("CONFIRMED");

        appointment = appointmentRepository.save(appointment);

        return AppointmentResponseDTO.builder()
                .id(appointment.getId())
                .medecinId(medecin.getId())
                .patientId(patient.getId())
                .date(appointment.getDate())
                .time(appointment.getTime())
                .status(appointment.getStatus())
                .build();
    }

    public Long getMedecinIdFromUserId(Long userId) {
        return medecinRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Médecin introuvable"))
                .getId();
    }

    public Long getMedecinIdFromSecretaireUserId(Long userId) {
        Secretaire secretaire = secretaireRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Secrétaire introuvable"));
        MedecinSecretaire relation = medecinSecretaireRepository
                .findBySecretaireUserIdAndStatus(secretaire.getUser().getId(), "ACTIVE")
                .orElseThrow(() -> new RuntimeException("Aucune relation active pour cette secrétaire"));
        return relation.getMedecin().getId();
    }
}
