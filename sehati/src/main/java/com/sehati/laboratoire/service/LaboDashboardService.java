package com.sehati.laboratoire.service;

import com.sehati.appointment.repository.AppointmentRepository;
import com.sehati.laboratoire.dto.LaboDashboardReviewDTO;
import com.sehati.laboratoire.dto.LaboDashboardStatsDTO;
import com.sehati.laboratoire.dto.LaboTrendPointDTO;
import com.sehati.laboratoire.entities.Laboratoire;
import com.sehati.laboratoire.repository.LaboratoireRepository;
import com.sehati.patient.repository.PatientRepository;
import com.sehati.review.dto.RatingSummaryDTO;
import com.sehati.review.entities.Review;
import com.sehati.review.entities.TargetType;
import com.sehati.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LaboDashboardService {

    private final LaboratoireRepository laboratoireRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final ReviewRepository reviewRepository;

    public LaboDashboardStatsDTO getDashboardStats(Long userId, String period) {
        Laboratoire labo = laboratoireRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Laboratoire non trouvé"));
        Long laboId = labo.getId();

        LocalDate now = LocalDate.now();
        LocalDate start;
        LocalDate end;

        switch (period.toUpperCase()) {
            case "MONTH":
                start = now.withDayOfMonth(1);
                end = now.withDayOfMonth(now.lengthOfMonth());
                break;
            case "YEAR":
                start = now.withDayOfYear(1);
                end = now.withDayOfYear(now.lengthOfYear());
                break;
            case "WEEK":
            default:
                WeekFields weekFields = WeekFields.of(Locale.FRANCE);
                start = now.with(weekFields.dayOfWeek(), 1); // Lundi
                end = now.with(weekFields.dayOfWeek(), 7);   // Dimanche
                break;
        }

        long appointmentCount = appointmentRepository.countByLaboratoireIdAndDateBetween(laboId, start, end);
        long completedAppointments = appointmentRepository.countByLaboratoireIdAndStatusAndDateBetween(laboId, "COMPLETED", start, end);
        long confirmedAppointments = appointmentRepository.countByLaboratoireIdAndStatusAndDateBetween(laboId, "CONFIRMED", start, end);
        long cancelledAppointments = appointmentRepository.countByLaboratoireIdAndStatusAndDateBetween(laboId, "CANCELLED", start, end);

        List<Object[]> rawTrend = appointmentRepository.countByLaboratoireIdGroupByDate(laboId, start, end);
        List<LaboTrendPointDTO> trend = buildTrendData(rawTrend, period, start, end);

        long totalPatients = patientRepository.countDistinctPatientsByLaboratoireId(laboId);

        RatingSummaryDTO ratingSummary = reviewRepository.getRatingSummary(laboId, TargetType.LABORATORY);
        Double avgRating = (ratingSummary != null && ratingSummary.getAverageRating() != null) ? ratingSummary.getAverageRating() : 0.0;
        long reviewCount = (ratingSummary != null && ratingSummary.getReviewCount() != null) ? ratingSummary.getReviewCount() : 0L;

        List<Review> recentReviewsData = reviewRepository.findRecentByTarget(laboId, TargetType.LABORATORY);
        List<LaboDashboardReviewDTO> recentReviews = recentReviewsData.stream().map(r -> {
            String reviewerName = r.getReviewer().getPrenom() + " " + r.getReviewer().getNom();
            return LaboDashboardReviewDTO.builder()
                    .id(r.getId())
                    .reviewerName(reviewerName)
                    .reviewerPhotoUrl(r.getReviewer().getPhotoProfilUrl())
                    .rating(r.getRating())
                    .comment(r.getComment())
                    .createdAt(r.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());

        return LaboDashboardStatsDTO.builder()
                .appointmentCount(appointmentCount)
                .completedAppointments(completedAppointments)
                .confirmedAppointments(confirmedAppointments)
                .cancelledAppointments(cancelledAppointments)
                .appointmentTrend(trend)
                .totalPatients(totalPatients)
                .averageRating(avgRating)
                .reviewCount(reviewCount)
                .recentReviews(recentReviews)
                .build();
    }

    private List<LaboTrendPointDTO> buildTrendData(List<Object[]> rawTrend, String period, LocalDate start, LocalDate end) {
        Map<String, Long> groupedData = new LinkedHashMap<>();

        if ("WEEK".equalsIgnoreCase(period)) {
            for (int i = 0; i < 7; i++) {
                LocalDate d = start.plusDays(i);
                String label = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.FRANCE);
                label = label.substring(0, 1).toUpperCase() + label.substring(1);
                groupedData.put(label, 0L);
            }
            for (Object[] row : rawTrend) {
                LocalDate date = getLocalDate(row[0]);
                Long count = ((Number) row[1]).longValue();
                String label = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.FRANCE);
                label = label.substring(0, 1).toUpperCase() + label.substring(1);
                groupedData.put(label, groupedData.getOrDefault(label, 0L) + count);
            }
        } else if ("MONTH".equalsIgnoreCase(period)) {
            int weeksInMonth = end.get(ChronoField.ALIGNED_WEEK_OF_MONTH);
            for (int i = 1; i <= weeksInMonth; i++) {
                groupedData.put("Sem " + i, 0L);
            }
            for (Object[] row : rawTrend) {
                LocalDate date = getLocalDate(row[0]);
                Long count = ((Number) row[1]).longValue();
                int weekOfMonth = date.get(ChronoField.ALIGNED_WEEK_OF_MONTH);
                String label = "Sem " + weekOfMonth;
                groupedData.put(label, groupedData.getOrDefault(label, 0L) + count);
            }
        } else if ("YEAR".equalsIgnoreCase(period)) {
            for (int i = 1; i <= 12; i++) {
                LocalDate d = LocalDate.of(start.getYear(), i, 1);
                String label = d.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRANCE);
                label = label.substring(0, 1).toUpperCase() + label.substring(1);
                groupedData.put(label, 0L);
            }
            for (Object[] row : rawTrend) {
                LocalDate date = getLocalDate(row[0]);
                Long count = ((Number) row[1]).longValue();
                String label = date.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRANCE);
                label = label.substring(0, 1).toUpperCase() + label.substring(1);
                groupedData.put(label, groupedData.getOrDefault(label, 0L) + count);
            }
        }

        return groupedData.entrySet().stream()
                .map(e -> new LaboTrendPointDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private LocalDate getLocalDate(Object sqlResult) {
        if (sqlResult instanceof java.sql.Date) {
            return ((java.sql.Date) sqlResult).toLocalDate();
        } else if (sqlResult instanceof LocalDate) {
            return (LocalDate) sqlResult;
        }
        throw new IllegalArgumentException("Unexpected date type: " + sqlResult.getClass());
    }
}
