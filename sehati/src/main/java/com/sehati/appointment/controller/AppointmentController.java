package com.sehati.appointment.controller;

import com.sehati.appointment.dto.AppointmentRequestDTO;
import com.sehati.appointment.dto.AppointmentResponseDTO;
import com.sehati.appointment.dto.AvailableDateDTO;
import com.sehati.appointment.dto.ConsultationAppointmentDTO;
import com.sehati.appointment.dto.OrdonnanceRequestDTO;
import com.sehati.appointment.dto.DemandeAnalyseRequestDTO;
import com.sehati.appointment.dto.PatientAppointmentDTO;
import com.sehati.appointment.dto.ScheduleMetaDTO;
import com.sehati.appointment.dto.TimeSlotDTO;
import com.sehati.appointment.service.AppointmentService;
import com.sehati.auth.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping("/available-dates/{medecinId}")
    public ResponseEntity<List<AvailableDateDTO>> getAvailableDates(@PathVariable Long medecinId) {
        return ResponseEntity.ok(appointmentService.getAvailableDates(medecinId));
    }

    @GetMapping("/available-slots/{medecinId}")
    public ResponseEntity<List<TimeSlotDTO>> getAvailableTimeSlots(
            @PathVariable Long medecinId,
            @RequestParam LocalDate date) {
        return ResponseEntity.ok(appointmentService.getAvailableTimeSlots(medecinId, date));
    }

    @GetMapping("/labo/available-dates/{laboId}")
    public ResponseEntity<List<AvailableDateDTO>> getAvailableLaboDates(@PathVariable Long laboId) {
        return ResponseEntity.ok(appointmentService.getAvailableLaboDates(laboId));
    }

    @GetMapping("/labo/available-slots/{laboId}")
    public ResponseEntity<List<TimeSlotDTO>> getAvailableLaboTimeSlots(
            @PathVariable Long laboId,
            @RequestParam LocalDate date) {
        return ResponseEntity.ok(appointmentService.getAvailableLaboTimeSlots(laboId, date));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<AppointmentResponseDTO> createAppointment(
            @RequestBody AppointmentRequestDTO request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        AppointmentResponseDTO response = appointmentService.createAppointment(userDetails.getId(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/block-slots")
    @PreAuthorize("hasAnyAuthority('MEDECIN', 'LABORATOIRE', 'SECRETAIRE')")
    public ResponseEntity<Void> blockSlots(
            @RequestBody com.sehati.appointment.dto.BlockSlotRequestDTO request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        // Extract role to pass to service
        String role = "MEDECIN";
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("LABORATOIRE"))) {
            role = "LABORATOIRE";
        } else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("SECRETAIRE"))) {
            role = "SECRETAIRE";
        }

        appointmentService.blockSlots(userDetails.getId(), request, role);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/weekly-blocks")
    @PreAuthorize("hasAnyAuthority('MEDECIN', 'LABORATOIRE', 'SECRETAIRE')")
    public ResponseEntity<List<com.sehati.appointment.dto.BlockedSlotDTO>> getWeeklyBlockedSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        String role = "MEDECIN";
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("LABORATOIRE"))) {
            role = "LABORATOIRE";
        } else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("SECRETAIRE"))) {
            role = "SECRETAIRE";
        }
                
        return ResponseEntity.ok(appointmentService.getWeeklyBlockedSlots(userDetails.getId(), role, start, end));
    }

    @DeleteMapping("/block-slots/{id}")
    @PreAuthorize("hasAnyAuthority('MEDECIN', 'LABORATOIRE', 'SECRETAIRE')")
    public ResponseEntity<Void> unblockSlot(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        String role = "MEDECIN";
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("LABORATOIRE"))) {
            role = "LABORATOIRE";
        } else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("SECRETAIRE"))) {
            role = "SECRETAIRE";
        }

        appointmentService.unblockSlot(id, userDetails.getId(), role);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-upcoming")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<List<PatientAppointmentDTO>> getMyUpcomingAppointments(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(appointmentService.getMyUpcomingAppointments(userDetails.getId()));
    }

    @GetMapping("/my-history")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<Page<PatientAppointmentDTO>> getMyPastAppointments(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(appointmentService.getMyPastAppointments(userDetails.getId(), PageRequest.of(page, size)));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<Void> cancelAppointment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        appointmentService.cancelAppointment(userDetails.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("hasAnyAuthority('MEDECIN', 'LABORATOIRE', 'SECRETAIRE')")
    public ResponseEntity<com.sehati.appointment.dto.AppointmentDetailDTO> getAppointmentDetails(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(appointmentService.getAppointmentDetails(id, userDetails.getId()));
    }

    @PutMapping("/{id}/provider-cancel")
    @PreAuthorize("hasAnyAuthority('MEDECIN', 'LABORATOIRE', 'SECRETAIRE')")
    public ResponseEntity<Void> providerCancelAppointment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        appointmentService.providerCancelAppointment(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyAuthority('MEDECIN', 'LABORATOIRE', 'SECRETAIRE')")
    public ResponseEntity<Void> completeAppointment(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, String> payload,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String notes = payload != null ? payload.get("notes") : null;
        appointmentService.completeAppointment(id, notes);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/ordonnance")
    @PreAuthorize("hasAuthority('MEDECIN')")
    public ResponseEntity<java.util.Map<String, String>> generateOrdonnance(
            @PathVariable Long id,
            @RequestBody OrdonnanceRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String ordonnanceUrl = appointmentService.generateOrdonnance(id, requestDTO);
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("ordonnanceUrl", ordonnanceUrl);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/ordonnance")
    @PreAuthorize("hasAuthority('MEDECIN')")
    public ResponseEntity<Void> deleteOrdonnance(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        appointmentService.deleteOrdonnance(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/demande-analyse")
    @PreAuthorize("hasAuthority('MEDECIN')")
    public ResponseEntity<java.util.Map<String, String>> generateDemandeAnalyse(
            @PathVariable Long id,
            @RequestBody DemandeAnalyseRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String demandeAnalyseUrl = appointmentService.generateDemandeAnalyse(id, requestDTO);
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("demandeAnalyseUrl", demandeAnalyseUrl);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/demande-analyse")
    @PreAuthorize("hasAuthority('MEDECIN')")
    public ResponseEntity<Void> deleteDemandeAnalyse(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        appointmentService.deleteDemandeAnalyse(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/result")
    @PreAuthorize("hasAuthority('LABORATOIRE')")
    public ResponseEntity<Void> updateResultUrl(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> payload) {
        String resultUrl = payload.get("resultUrl");
        appointmentService.updateResultUrl(id, resultUrl);
        return ResponseEntity.noContent().build();
    }

    // =========================================================

    // Consultation des rendez-vous : Médecin
    // =========================================================

    @GetMapping("/medecin")
    @PreAuthorize("hasAuthority('MEDECIN')")
    public ResponseEntity<List<ConsultationAppointmentDTO>> getMedecinAppointments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForMedecin(userDetails.getId(), date));
    }

    @GetMapping("/medecin/weekly")
    @PreAuthorize("hasAuthority('MEDECIN')")
    public ResponseEntity<List<ConsultationAppointmentDTO>> getMedecinWeeklyAppointments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(appointmentService.getWeeklyAppointmentsForMedecin(userDetails.getId(), start, end));
    }

    @GetMapping("/medecin/schedule-meta")
    @PreAuthorize("hasAuthority('MEDECIN')")
    public ResponseEntity<ScheduleMetaDTO> getMedecinScheduleMeta(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(appointmentService.getScheduleMetaForMedecin(userDetails.getId()));
    }

    // =========================================================
    // Consultation des rendez-vous : Secrétaire
    // =========================================================

    @GetMapping("/secretaire")
    @PreAuthorize("hasAuthority('SECRETAIRE')")
    public ResponseEntity<List<ConsultationAppointmentDTO>> getSecretaireAppointments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForSecretaire(userDetails.getId(), date));
    }

    @GetMapping("/secretaire/weekly")
    @PreAuthorize("hasAuthority('SECRETAIRE')")
    public ResponseEntity<List<ConsultationAppointmentDTO>> getSecretaireWeeklyAppointments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(appointmentService.getWeeklyAppointmentsForSecretaire(userDetails.getId(), start, end));
    }

    @GetMapping("/secretaire/schedule-meta")
    @PreAuthorize("hasAuthority('SECRETAIRE')")
    public ResponseEntity<ScheduleMetaDTO> getSecretaireScheduleMeta(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(appointmentService.getScheduleMetaForSecretaire(userDetails.getId()));
    }

    // =========================================================
    // Consultation des rendez-vous : Laboratoire
    // =========================================================

    @GetMapping("/labo")
    @PreAuthorize("hasAuthority('LABORATOIRE')")
    public ResponseEntity<List<ConsultationAppointmentDTO>> getLaboAppointments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForLabo(userDetails.getId(), date));
    }

    @GetMapping("/labo/weekly")
    @PreAuthorize("hasAuthority('LABORATOIRE')")
    public ResponseEntity<List<ConsultationAppointmentDTO>> getLaboWeeklyAppointments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(appointmentService.getWeeklyAppointmentsForLabo(userDetails.getId(), start, end));
    }

    @GetMapping("/labo/schedule-meta")
    @PreAuthorize("hasAuthority('LABORATOIRE')")
    public ResponseEntity<ScheduleMetaDTO> getLaboScheduleMeta(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(appointmentService.getScheduleMetaForLabo(userDetails.getId()));
    }

    // =========================================================
    // Création manuelle de RDV (médecin ou secrétaire)
    // =========================================================

    @PostMapping("/manual-create")
    @PreAuthorize("hasAnyAuthority('MEDECIN', 'SECRETAIRE')")
    public ResponseEntity<AppointmentResponseDTO> createManualAppointment(
            @RequestBody com.sehati.appointment.dto.ManualAppointmentRequestDTO request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        String role = "MEDECIN";
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("SECRETAIRE"))) {
            role = "SECRETAIRE";
        }

        AppointmentResponseDTO response = appointmentService.createManualAppointment(userDetails.getId(), role, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @GetMapping("/my-available-slots")
    @PreAuthorize("hasAnyAuthority('MEDECIN', 'SECRETAIRE')")
    public ResponseEntity<List<com.sehati.appointment.dto.TimeSlotDTO>> getMyAvailableSlots(
            @RequestParam LocalDate date,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        String role = "MEDECIN";
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("SECRETAIRE"))) {
            role = "SECRETAIRE";
        }
        
        Long medecinId;
        if ("MEDECIN".equals(role)) {
            medecinId = appointmentService.getMedecinIdFromUserId(userDetails.getId());
        } else {
            medecinId = appointmentService.getMedecinIdFromSecretaireUserId(userDetails.getId());
        }
        
        return ResponseEntity.ok(appointmentService.getAvailableTimeSlots(medecinId, date));
    }
}
