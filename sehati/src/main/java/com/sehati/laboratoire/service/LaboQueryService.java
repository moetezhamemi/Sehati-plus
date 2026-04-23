package com.sehati.laboratoire.service;

import com.sehati.laboratoire.dto.LaboDetailDTO;
import com.sehati.laboratoire.dto.LaboSummaryDTO;
import com.sehati.laboratoire.entities.Laboratoire;
import com.sehati.laboratoire.repository.LaboratoireRepository;
import com.sehati.common.dto.DayScheduleDTO;
import com.sehati.common.dto.WorkHoursDTO;
import com.sehati.common.entities.DaySchedule;
import com.sehati.common.entities.PhoneNumber;
import com.sehati.common.entities.WorkHours;
import com.sehati.review.dto.RatingSummaryDTO;
import com.sehati.review.entities.TargetType;
import com.sehati.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LaboQueryService {

    private static final Logger logger = LoggerFactory.getLogger(LaboQueryService.class);

    private final LaboratoireRepository laboratoireRepository;
    private final ReviewRepository reviewRepository;

    public Page<LaboSummaryDTO> getApprovedLabos(Pageable pageable) {
        logger.info("Fetching approved labs - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return laboratoireRepository.findAllApprovedAndEnabled(pageable)
                .map(this::mapToSummaryDTO);
    }

    public Page<LaboSummaryDTO> searchLabos(String search, String ville, Pageable pageable) {
        logger.info("Searching labos - Page: {}, Size: {}, Search: {}, Ville: {}", 
                     pageable.getPageNumber(), pageable.getPageSize(), search, ville);
        return laboratoireRepository.searchApproved(search, ville, pageable)
                .map(this::mapToSummaryDTO);
    }

    public LaboDetailDTO getLaboDetail(Long id) {
        Laboratoire labo = laboratoireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Laboratoire non trouvé avec l'id : " + id));
        return mapToDetailDTO(labo);
    }

    private LaboDetailDTO mapToDetailDTO(Laboratoire labo) {
        RatingSummaryDTO ratingSummary = reviewRepository.getRatingSummary(labo.getId(), TargetType.LABORATORY);

        Double avg = (ratingSummary != null && ratingSummary.getAverageRating() != null) ? ratingSummary.getAverageRating() : 0.0;
        Long count = (ratingSummary != null && ratingSummary.getReviewCount() != null) ? ratingSummary.getReviewCount() : 0L;

        return LaboDetailDTO.builder()
                .id(labo.getId())
                .nomLabo(labo.getNomLabo())
                .adresseComplete(labo.getAdresseComplete())
                .ville(labo.getVille())
                .latitude(labo.getLatitude())
                .longitude(labo.getLongitude())
                .phones(labo.getPhones() != null ? labo.getPhones().stream().map(PhoneNumber::getNumber).collect(java.util.stream.Collectors.toList()) : new java.util.ArrayList<>())
                .email(labo.getUser() != null ? labo.getUser().getEmail() : null)
                .analyses(labo.getAnalyses())
                .photoProfilUrl(labo.getPhotoProfilUrl())
                .responsable(labo.getResponsable())
                .workHours(mapWorkHoursToDTO(labo.getWorkHours()))
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

    private LaboSummaryDTO mapToSummaryDTO(Laboratoire labo) {
        try {
            RatingSummaryDTO ratingSummary = reviewRepository.getRatingSummary(labo.getId(), TargetType.LABORATORY);

            Double avg = (ratingSummary != null && ratingSummary.getAverageRating() != null)
                    ? ratingSummary.getAverageRating() : 0.0;
            Long count = (ratingSummary != null && ratingSummary.getReviewCount() != null)
                    ? ratingSummary.getReviewCount() : 0L;

            return LaboSummaryDTO.builder()
                    .id(labo.getId())
                    .nomLabo(labo.getNomLabo())
                    .adresseComplete(labo.getAdresseComplete())
                    .ville(labo.getVille())
                    .phones(labo.getPhones() != null ? labo.getPhones().stream().map(PhoneNumber::getNumber).collect(java.util.stream.Collectors.toList()) : new java.util.ArrayList<>())
                    .photoProfilUrl(labo.getPhotoProfilUrl())
                    .averageRating(avg)
                    .reviewCount(count)
                    .build();
        } catch (Exception e) {
            logger.error("Error mapping labo summary for ID {}: {}", labo.getId(), e.getMessage());
            return LaboSummaryDTO.builder()
                    .id(labo.getId())
                    .nomLabo(labo.getNomLabo())
                    .adresseComplete(labo.getAdresseComplete())
                    .ville(labo.getVille())
                    .averageRating(0.0)
                    .reviewCount(0L)
                    .build();
        }
    }
}
