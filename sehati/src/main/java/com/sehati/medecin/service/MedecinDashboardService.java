package com.sehati.medecin.service;

import com.sehati.appointment.repository.AppointmentRepository;
import com.sehati.medecin.dto.DashboardReviewDTO;
import com.sehati.medecin.dto.DashboardStatsDTO;
import com.sehati.medecin.dto.TrendPointDTO;
import com.sehati.medecin.entities.Medecin;
import com.sehati.medecin.repository.MedecinRepository;
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
public class MedecinDashboardService {

    private final MedecinRepository medecinRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final ReviewRepository reviewRepository;

    public DashboardStatsDTO getDashboardStats(Long userId, String period) {
        Medecin medecin = medecinRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));
        Long medecinId = medecin.getId();

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
                start = now.with(weekFields.dayOfWeek(), 1); // Monday
                end = now.with(weekFields.dayOfWeek(), 7); // Sunday
                break;
        }

        long appointmentCount = appointmentRepository.countByMedecinIdAndDateBetween(medecinId, start, end);
        long consultationCount = appointmentRepository.countByMedecinIdAndStatusAndDateBetween(medecinId, "COMPLETED", start, end);
        long completedAppointments = appointmentRepository.countByMedecinIdAndStatusAndDateBetween(medecinId, "COMPLETED", start, end);
        long confirmedAppointments = appointmentRepository.countByMedecinIdAndStatusAndDateBetween(medecinId, "CONFIRMED", start, end);
        long cancelledAppointments = appointmentRepository.countByMedecinIdAndStatusAndDateBetween(medecinId, "CANCELLED", start, end);

        List<Object[]> rawTrend = appointmentRepository.countByMedecinIdGroupByDate(medecinId, start, end);
        List<TrendPointDTO> trend = buildTrendData(rawTrend, period, start, end);

        long totalPatients = patientRepository.countDistinctPatientsByMedecinId(medecinId);

        RatingSummaryDTO ratingSummary = reviewRepository.getRatingSummary(medecinId, TargetType.MEDECIN);
        Double avgRating = (ratingSummary != null && ratingSummary.getAverageRating() != null) ? ratingSummary.getAverageRating() : 0.0;
        long reviewCount = (ratingSummary != null && ratingSummary.getReviewCount() != null) ? ratingSummary.getReviewCount() : 0L;

        List<Review> recentReviewsData = reviewRepository.findRecentByTarget(medecinId, TargetType.MEDECIN);
        List<DashboardReviewDTO> recentReviews = recentReviewsData.stream().map(r -> {
            String reviewerName = r.getReviewer().getPrenom() + " " + r.getReviewer().getNom();
            return DashboardReviewDTO.builder()
                    .id(r.getId())
                    .reviewerName(reviewerName)
                    .reviewerPhotoUrl(r.getReviewer().getPhotoProfilUrl())
                    .rating(r.getRating())
                    .comment(r.getComment())
                    .createdAt(r.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());

        return DashboardStatsDTO.builder()
                .appointmentCount(appointmentCount)
                .consultationCount(consultationCount)
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

    private List<TrendPointDTO> buildTrendData(List<Object[]> rawTrend, String period, LocalDate start, LocalDate end) {
        Map<String, Long> groupedData = new LinkedHashMap<>();
        
        if ("WEEK".equalsIgnoreCase(period)) {
            // Init map with all days of week
            for (int i = 0; i < 7; i++) {
                LocalDate d = start.plusDays(i);
                String label = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.FRANCE);
                // Capitalize first letter
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
            // Group by week of month
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
            // Group by month
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
                .map(e -> new TrendPointDTO(e.getKey(), e.getValue()))
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
