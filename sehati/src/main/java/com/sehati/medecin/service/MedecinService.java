package com.sehati.medecin.service;

import com.sehati.auth.dto.SignupMedecinRequest;
import com.sehati.auth.entities.User;
import com.sehati.common.dto.DayScheduleDTO;
import com.sehati.common.dto.WorkHoursDTO;
import com.sehati.common.entities.DaySchedule;
import com.sehati.common.entities.PhoneNumber;
import com.sehati.common.entities.WorkHours;
import com.sehati.common.service.CloudinaryService;
import com.sehati.medecin.dto.HoraireDTO;
import com.sehati.medecin.dto.MedecinDetailDTO;
import com.sehati.medecin.dto.MedecinProfileDTO;
import com.sehati.medecin.dto.MedecinSummaryDTO;
import com.sehati.medecin.entities.Medecin;
import com.sehati.medecin.repository.MedecinRepository;
import com.sehati.review.dto.RatingSummaryDTO;
import com.sehati.review.entities.TargetType;
import com.sehati.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedecinService {

    private static final Logger logger = LoggerFactory.getLogger(MedecinService.class);

    private final MedecinRepository medecinRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public void createMedecinProfile(User savedUser, SignupMedecinRequest request) {
        Medecin medecin = new Medecin();
        medecin.setNom(request.getNom());
        medecin.setPrenom(request.getPrenom());
        medecin.setSpecialite(request.getSpecialite());
        medecin.setAdresseCabinet(request.getAdresseCabinet());
        medecin.setVille(request.getVille());

        if (request.getPhones() != null) {
            java.util.List<PhoneNumber> phoneNumbers = new java.util.ArrayList<>();
            for (String p : request.getPhones()) {
                PhoneNumber pn = new PhoneNumber(p);
                pn.setMedecin(medecin);
                phoneNumbers.add(pn);
            }
            medecin.setPhones(phoneNumbers);
        }

        medecin.setDiplomeUrl(request.getDiplomeUrl());
        medecin.setLatitude(request.getLatitude());
        medecin.setLongitude(request.getLongitude());
        medecin.setUser(savedUser);
        medecinRepository.save(medecin);
    }

    public boolean exists(Long id) {
        return medecinRepository.existsById(id);
    }

    public Page<MedecinSummaryDTO> getApprovedDoctors(Pageable pageable) {
        logger.info("Fetching approved doctors - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return medecinRepository.findAllApprovedAndEnabled(pageable).map(this::mapToSummaryDTO);
    }

    public Page<MedecinSummaryDTO> searchDoctors(String search, String ville, String specialite, Pageable pageable) {
        logger.info("Searching doctors - Page: {}, Size: {}, Search: {}, Ville: {}, Specialite: {}",
                pageable.getPageNumber(), pageable.getPageSize(), search, ville, specialite);
        return medecinRepository.searchApproved(search, ville, specialite, pageable).map(this::mapToSummaryDTO);
    }

    public List<String> getDistinctVilles() {
        return medecinRepository.findDistinctVilles();
    }

    public List<String> getDistinctSpecialites() {
        return medecinRepository.findDistinctSpecialites();
    }

    public MedecinDetailDTO getMedecinDetail(Long id) {
        Medecin medecin = medecinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé avec l'id : " + id));
        return mapToDetailDTO(medecin);
    }

    private MedecinDetailDTO mapToDetailDTO(Medecin medecin) {
        RatingSummaryDTO ratingSummary = reviewRepository.getRatingSummary(medecin.getId(), TargetType.MEDECIN);
        Double avg   = (ratingSummary != null && ratingSummary.getAverageRating() != null) ? ratingSummary.getAverageRating() : 0.0;
        Long   count = (ratingSummary != null && ratingSummary.getReviewCount() != null)   ? ratingSummary.getReviewCount()   : 0L;

        return MedecinDetailDTO.builder()
                .id(medecin.getId())
                .nom(medecin.getNom())
                .prenom(medecin.getPrenom())
                .specialite(medecin.getSpecialite())
                .adresseCabinet(medecin.getAdresseCabinet())
                .ville(medecin.getVille())
                .phones(medecin.getPhones() != null
                        ? medecin.getPhones().stream().map(PhoneNumber::getNumber).collect(java.util.stream.Collectors.toList())
                        : new java.util.ArrayList<>())
                .photoProfilUrl(medecin.getPhotoProfilUrl())
                .biographie(medecin.getBiographie())
                .workHours(mapWorkHoursToDTO(medecin.getWorkHours()))
                .latitude(medecin.getLatitude())
                .longitude(medecin.getLongitude())
                .averageRating(avg)
                .reviewCount(count)
                .build();
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

    private MedecinSummaryDTO mapToSummaryDTO(Medecin medecin) {
        try {
            RatingSummaryDTO ratingSummary = reviewRepository.getRatingSummary(medecin.getId(), TargetType.MEDECIN);
            Double avg   = (ratingSummary != null && ratingSummary.getAverageRating() != null) ? ratingSummary.getAverageRating() : 0.0;
            Long   count = (ratingSummary != null && ratingSummary.getReviewCount() != null)   ? ratingSummary.getReviewCount()   : 0L;

            return MedecinSummaryDTO.builder()
                    .id(medecin.getId())
                    .nom(medecin.getNom())
                    .prenom(medecin.getPrenom())
                    .specialite(medecin.getSpecialite())
                    .adresseCabinet(medecin.getAdresseCabinet())
                    .ville(medecin.getVille())
                    .phones(medecin.getPhones() != null
                            ? medecin.getPhones().stream().map(PhoneNumber::getNumber).collect(java.util.stream.Collectors.toList())
                            : new java.util.ArrayList<>())
                    .photoProfilUrl(medecin.getPhotoProfilUrl())
                    .averageRating(avg)
                    .reviewCount(count)
                    .build();
        } catch (Exception e) {
            logger.error("Error mapping doctor summary for ID {}: {}", medecin.getId(), e.getMessage());
            return MedecinSummaryDTO.builder()
                    .id(medecin.getId()).nom(medecin.getNom()).prenom(medecin.getPrenom())
                    .specialite(medecin.getSpecialite()).averageRating(0.0).reviewCount(0L).build();
        }
    }

    // ==================== Profile Management ====================

    public MedecinProfileDTO getMedecinProfile(Long userId) {
        Medecin medecin = medecinRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));
        return toProfileDTO(medecin);
    }

    @Transactional
    public MedecinProfileDTO updateMedecinProfile(Long userId, MedecinProfileDTO dto) {
        Medecin medecin = medecinRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));
        if (dto.getNom()       != null) medecin.setNom(dto.getNom());
        if (dto.getPrenom()    != null) medecin.setPrenom(dto.getPrenom());
        if (dto.getBiographie() != null) medecin.setBiographie(dto.getBiographie());
        
        // Update Cabinet Fields
        if (dto.getAdresseCabinet() != null) medecin.setAdresseCabinet(dto.getAdresseCabinet());
        if (dto.getVille() != null) medecin.setVille(dto.getVille());
        if (dto.getLatitude() != null) medecin.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) medecin.setLongitude(dto.getLongitude());
        
        if (dto.getPhones() != null) {
            medecin.getPhones().clear();
            for (String p : dto.getPhones()) {
                PhoneNumber pn = new PhoneNumber(p);
                pn.setMedecin(medecin);
                medecin.getPhones().add(pn);
            }
        }
        
        medecinRepository.save(medecin);
        return toProfileDTO(medecin);
    }

    @Transactional
    public void updateMedecinPhoto(Long userId, String newPhotoUrl, CloudinaryService cloudinaryService) {
        Medecin medecin = medecinRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));
        String oldUrl = medecin.getPhotoProfilUrl();
        if (oldUrl != null && !oldUrl.isBlank()) cloudinaryService.deleteFile(oldUrl);
        medecin.setPhotoProfilUrl(newPhotoUrl);
        medecinRepository.save(medecin);
    }

    @Transactional
    public void updateSignature(Long userId, String newUrl, CloudinaryService cloudinaryService) {
        Medecin medecin = medecinRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));
        String oldUrl = medecin.getSignatureUrl();
        if (oldUrl != null && !oldUrl.isBlank()) cloudinaryService.deleteFile(oldUrl);
        medecin.setSignatureUrl(newUrl);
        medecinRepository.save(medecin);
    }

    // ==================== Horaire d'Ouverture ====================

    public HoraireDTO getHoraire(Long userId) {
        Medecin medecin = medecinRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));
        return HoraireDTO.builder()
                .consultationTime(medecin.getConsultationTime())
                .workHours(mapWorkHoursToDTO(medecin.getWorkHours()))
                .build();
    }

    @Transactional
    public HoraireDTO updateHoraire(Long userId, HoraireDTO dto) {
        Medecin medecin = medecinRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));

        if (dto.getConsultationTime() != null) {
            medecin.setConsultationTime(dto.getConsultationTime());
        }

        if (dto.getWorkHours() != null) {
            WorkHours wh = medecin.getWorkHours();
            if (wh == null) {
                wh = new WorkHours();
            }
            WorkHoursDTO whDto = dto.getWorkHours();
            wh.setLundi(mapDayFromDTO(whDto.getLundi()));
            wh.setMardi(mapDayFromDTO(whDto.getMardi()));
            wh.setMercredi(mapDayFromDTO(whDto.getMercredi()));
            wh.setJeudi(mapDayFromDTO(whDto.getJeudi()));
            wh.setVendredi(mapDayFromDTO(whDto.getVendredi()));
            wh.setSamedi(mapDayFromDTO(whDto.getSamedi()));
            wh.setDimanche(mapDayFromDTO(whDto.getDimanche()));
            medecin.setWorkHours(wh);
        }

        medecinRepository.save(medecin);
        return HoraireDTO.builder()
                .consultationTime(medecin.getConsultationTime())
                .workHours(mapWorkHoursToDTO(medecin.getWorkHours()))
                .build();
    }

    private DaySchedule mapDayFromDTO(DayScheduleDTO dto) {
        if (dto == null) return null;
        return DaySchedule.builder()
                .debut(dto.getDebut())
                .fin(dto.getFin())
                .ferme(dto.isFerme())
                .build();
    }

    @Transactional
    public void updateCachet(Long userId, String newUrl, CloudinaryService cloudinaryService) {
        Medecin medecin = medecinRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));
        String oldUrl = medecin.getCachetUrl();
        if (oldUrl != null && !oldUrl.isBlank()) cloudinaryService.deleteFile(oldUrl);
        medecin.setCachetUrl(newUrl);
        medecinRepository.save(medecin);
    }

    private MedecinProfileDTO toProfileDTO(Medecin medecin) {
        String email = medecin.getUser() != null ? medecin.getUser().getEmail() : null;
        return MedecinProfileDTO.builder()
                .nom(medecin.getNom())
                .prenom(medecin.getPrenom())
                .email(email)
                .specialite(medecin.getSpecialite())
                .biographie(medecin.getBiographie())
                .photoProfilUrl(medecin.getPhotoProfilUrl())
                .signatureUrl(medecin.getSignatureUrl())
                .cachetUrl(medecin.getCachetUrl())
                .adresseCabinet(medecin.getAdresseCabinet())
                .ville(medecin.getVille())
                .latitude(medecin.getLatitude())
                .longitude(medecin.getLongitude())
                .phones(medecin.getPhones() != null
                    ? medecin.getPhones().stream().map(PhoneNumber::getNumber).collect(java.util.stream.Collectors.toList())
                    : java.util.Collections.emptyList())
                .build();
    }
}
