package com.sehati.admin.service;
import com.sehati.admin.dto.*;
import com.sehati.admin.repository.AdminUserRepository;
import com.sehati.auth.entities.User;
import com.sehati.common.dto.DayScheduleDTO;
import com.sehati.common.dto.WorkHoursDTO;
import com.sehati.common.entities.DaySchedule;
import com.sehati.common.entities.PhoneNumber;
import com.sehati.common.entities.WorkHours;
import com.sehati.laboratoire.repository.LaboratoireRepository;
import com.sehati.medecin.repository.MedecinRepository;
import com.sehati.patient.repository.PatientRepository;
import com.sehati.review.dto.RatingSummaryDTO;
import com.sehati.review.entities.TargetType;
import com.sehati.review.repository.ReviewRepository;
import com.sehati.secretaire.entities.MedecinSecretaire;
import com.sehati.secretaire.repository.MedecinSecretaireRepository;
import com.sehati.secretaire.repository.SecretaireRepository;
import com.sehati.auth.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserServiceImpl implements AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final MedecinRepository medecinRepository;
    private final PatientRepository patientRepository;
    private final LaboratoireRepository laboratoireRepository;
    private final SecretaireRepository secretaireRepository;
    private final MedecinSecretaireRepository medecinSecretaireRepository;
    private final ReviewRepository reviewRepository;
    private final EmailService emailService;

    @Override
    public Page<UserAdminProjection> getAllUsers(String search, String role, String status, String specialite, Pageable pageable) {
        return adminUserRepository.findAllUsersWithFilters(search, role, status, specialite, pageable);
    }

    @Override
    public Page<UserAdminProjection> getPendingUsers(String search, String role, String specialite, Pageable pageable) {
        return adminUserRepository.findAllPendingUsersWithFilters(search, role, specialite, pageable);
    }

    @Override
    public AdminUserDetailDTO getUserDetail(Long userId) {
        User user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + userId));

        String role = user.getRoles() != null && !user.getRoles().isEmpty()
                ? user.getRoles().get(0).getName()
                : "UNKNOWN";

        switch (role) {
            case "MEDECIN":
                return buildMedecinDetail(user, role);
            case "PATIENT":
                return buildPatientDetail(user, role);
            case "LABORATOIRE":
                return buildLaboDetail(user, role);
            case "SECRETAIRE":
                return buildSecretaireDetail(user, role);
            default:
                // Fallback: return base DTO
                AdminUserDetailDTO base = new AdminUserDetailDTO();
                base.setUserId(user.getId());
                base.setRole(role);
                base.setStatus(user.getStatus());
                base.setEmail(user.getEmail());
                return base;
        }
    }

    // ==================== Patient ====================

    private AdminPatientDetailDTO buildPatientDetail(User user, String role) {
        AdminPatientDetailDTO dto = new AdminPatientDetailDTO();
        fillCommonFields(dto, user, role);

        patientRepository.findByUserId(user.getId()).ifPresent(patient -> {
            dto.setNom(patient.getNom());
            dto.setPrenom(patient.getPrenom());
            dto.setDateNaissance(patient.getDateNaissance());
            dto.setTelephone(patient.getTelephone());
            dto.setPhotoProfilUrl(patient.getPhotoProfilUrl());
        });

        return dto;
    }

    // ==================== Médecin ====================

    private AdminMedecinDetailDTO buildMedecinDetail(User user, String role) {
        AdminMedecinDetailDTO dto = new AdminMedecinDetailDTO();
        fillCommonFields(dto, user, role);

        medecinRepository.findByUserId(user.getId()).ifPresent(medecin -> {
            dto.setEntityId(medecin.getId());
            dto.setNom(medecin.getNom());
            dto.setPrenom(medecin.getPrenom());
            dto.setSpecialite(medecin.getSpecialite() != null ? medecin.getSpecialite().getNom() : null);
            dto.setAdresseCabinet(medecin.getAdresseCabinet());
            dto.setVille(medecin.getVille());
            dto.setLatitude(medecin.getLatitude());
            dto.setLongitude(medecin.getLongitude());
            dto.setPhones(mapPhones(medecin.getPhones()));
            dto.setBiographie(medecin.getBiographie());
            dto.setWorkHours(mapWorkHoursToDTO(medecin.getWorkHours()));
            dto.setConsultationTime(medecin.getConsultationTime());
            dto.setDiplomeUrl(medecin.getDiplomeUrl());
            dto.setPhotoProfilUrl(medecin.getPhotoProfilUrl());

            // Reviews
            RatingSummaryDTO ratingSummary = reviewRepository.getRatingSummary(medecin.getId(), TargetType.MEDECIN);
            dto.setAverageRating(ratingSummary != null && ratingSummary.getAverageRating() != null ? ratingSummary.getAverageRating() : 0.0);
            dto.setReviewCount(ratingSummary != null && ratingSummary.getReviewCount() != null ? ratingSummary.getReviewCount() : 0L);
        });

        return dto;
    }

    // ==================== Laboratoire ====================

    private AdminLaboDetailDTO buildLaboDetail(User user, String role) {
        AdminLaboDetailDTO dto = new AdminLaboDetailDTO();
        fillCommonFields(dto, user, role);

        laboratoireRepository.findByUserId(user.getId()).ifPresent(labo -> {
            dto.setEntityId(labo.getId());
            dto.setNomLabo(labo.getNomLabo());
            dto.setAdresseComplete(labo.getAdresseComplete());
            dto.setVille(labo.getVille());
            dto.setLatitude(labo.getLatitude());
            dto.setLongitude(labo.getLongitude());
            dto.setPhones(mapPhones(labo.getPhones()));
            dto.setResponsable(labo.getResponsable());
            dto.setAnalyses(labo.getAnalyses() != null ? new ArrayList<>(labo.getAnalyses()) : new ArrayList<>());
            dto.setWorkHours(mapWorkHoursToDTO(labo.getWorkHours()));
            dto.setConsultationTime(labo.getConsultationTime());
            dto.setCapaciteParCreneau(labo.getCapaciteParCreneau());
            dto.setRegistreCommerceUrl(labo.getRegistreCommerceUrl());
            dto.setPhotoProfilUrl(labo.getPhotoProfilUrl());

            // Reviews
            RatingSummaryDTO ratingSummary = reviewRepository.getRatingSummary(labo.getId(), TargetType.LABORATORY);
            dto.setAverageRating(ratingSummary != null && ratingSummary.getAverageRating() != null ? ratingSummary.getAverageRating() : 0.0);
            dto.setReviewCount(ratingSummary != null && ratingSummary.getReviewCount() != null ? ratingSummary.getReviewCount() : 0L);
        });

        return dto;
    }

    // ==================== Secrétaire ====================

    private AdminSecretaireDetailDTO buildSecretaireDetail(User user, String role) {
        AdminSecretaireDetailDTO dto = new AdminSecretaireDetailDTO();
        fillCommonFields(dto, user, role);

        secretaireRepository.findByUserId(user.getId()).ifPresent(secretaire -> {
            dto.setNom(secretaire.getNom());
            dto.setPrenom(secretaire.getPrenom());

            // Find active association
            List<MedecinSecretaire> relations = medecinSecretaireRepository.findBySecretaireUserId(user.getId());
            relations.stream()
                    .filter(r -> "ACTIVE".equals(r.getStatus()))
                    .findFirst()
                    .ifPresent(activeRelation -> {
                        dto.setAssociatedDoctorName("Dr. " + activeRelation.getMedecin().getPrenom() + " " + activeRelation.getMedecin().getNom());
                        dto.setAssociatedSince(activeRelation.getCreatedAt());
                    });
        });

        return dto;
    }

    // ==================== Helpers ====================

    private void fillCommonFields(AdminUserDetailDTO dto, User user, String role) {
        dto.setUserId(user.getId());
        dto.setRole(role);
        dto.setStatus(user.getStatus());
        dto.setEmail(user.getEmail());
        dto.setEnabled(user.getEnabled());
    }

    private List<String> mapPhones(List<PhoneNumber> phones) {
        if (phones == null) return new ArrayList<>();
        return phones.stream().map(PhoneNumber::getNumber).collect(Collectors.toList());
    }

    private WorkHoursDTO mapWorkHoursToDTO(WorkHours workHours) {
        if (workHours == null) return null;
        return WorkHoursDTO.builder()
                .lundi(mapDayToDTO(workHours.getLundi()))
                .mardi(mapDayToDTO(workHours.getMardi()))
                .mercredi(mapDayToDTO(workHours.getMercredi()))
                .jeudi(mapDayToDTO(workHours.getJeudi()))
                .vendredi(mapDayToDTO(workHours.getVendredi()))
                .samedi(mapDayToDTO(workHours.getSamedi()))
                .dimanche(mapDayToDTO(workHours.getDimanche()))
                .build();
    }

    private DayScheduleDTO mapDayToDTO(DaySchedule day) {
        if (day == null) return null;
        return DayScheduleDTO.builder()
                .debut(day.getDebut())
                .fin(day.getFin())
                .ferme(day.isFerme())
                .build();
    }

    @Override
    @Transactional
    public void updateUserStatus(Long userId, boolean enabled) {
        User user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + userId));

        user.setEnabled(enabled);
        adminUserRepository.save(user);

        if (enabled) {
            emailService.sendAccountActivatedEmail(user.getEmail());
        } else {
            emailService.sendAccountDeactivatedEmail(user.getEmail());
        }
    }

    @Override
    @Transactional
    public void approveRequest(Long userId) {
        User user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + userId));

        user.setStatus("APPROVED");
        user.setEnabled(true);
        adminUserRepository.save(user);

        emailService.sendRequestApprovedEmail(user.getEmail());
    }

    @Override
    @Transactional
    public void rejectRequest(Long userId) {
        User user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + userId));

        user.setStatus("REJECTED");
        // enabled reste inchangé (false)
        adminUserRepository.save(user);

        emailService.sendRequestRejectedEmail(user.getEmail());
    }
}
