package com.sehati.admin.service;

import com.sehati.admin.dto.*;
import com.sehati.admin.repository.AdminUserRepository;
import com.sehati.appointment.repository.AppointmentRepository;
import com.sehati.laboratoire.repository.LaboratoireRepository;
import com.sehati.medecin.repository.MedecinRepository;
import com.sehati.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final AdminUserRepository adminUserRepository;
    private final MedecinRepository medecinRepository;
    private final LaboratoireRepository laboratoireRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    private static final String[] MONTH_LABELS = {
        "Jan", "Fév", "Mar", "Avr", "Mai", "Jun",
        "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc"
    };

    // ==================== Endpoint unifié ====================

    @Override
    public AdminDashboardOverviewDTO getOverview() {
        return AdminDashboardOverviewDTO.builder()
                .stats(getGlobalStats())
                .quality(getQualityStats())
                .pending(getPendingRequests())
                .citiesDistribution(getCitiesDistribution())
                .build();
    }

    // ==================== Stats globales ====================

    @Override
    public AdminDashboardStatsDTO getGlobalStats() {
        long totalMedecins    = medecinRepository.count();
        long totalLabos       = laboratoireRepository.count();
        long totalPatients    = patientRepository.count();
        long totalUsers       = totalMedecins + totalLabos + totalPatients;
        long totalAppointments = appointmentRepository.count();

        LocalDate now              = LocalDate.now();
        LocalDate startOfMonth     = now.withDayOfMonth(1);
        LocalDate endOfMonth       = now.withDayOfMonth(now.lengthOfMonth());
        LocalDate startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDate endOfLastMonth   = startOfMonth.minusDays(1);

        long appointmentsThisMonth = appointmentRepository.countByDateBetween(startOfMonth, endOfMonth);
        long appointmentsLastMonth = appointmentRepository.countByDateBetween(startOfLastMonth, endOfLastMonth);

        // Tendance : % évolution vs mois précédent
        Double appointmentsTrend = null;
        if (appointmentsLastMonth > 0) {
            double trend = ((appointmentsThisMonth - appointmentsLastMonth) * 100.0) / appointmentsLastMonth;
            appointmentsTrend = Math.round(trend * 10.0) / 10.0;
        } else if (appointmentsThisMonth > 0) {
            appointmentsTrend = 100.0; // Première activité ce mois
        }

        long cancelledCount    = appointmentRepository.countByStatus("CANCELLED");
        double cancellationRate = totalAppointments > 0
                ? Math.round((cancelledCount * 100.0 / totalAppointments) * 10.0) / 10.0
                : 0.0;

        return AdminDashboardStatsDTO.builder()
                .totalUsers(totalUsers)
                .totalMedecins(totalMedecins)
                .totalLaboratoires(totalLabos)
                .totalPatients(totalPatients)
                .totalAppointments(totalAppointments)
                .appointmentsThisMonth(appointmentsThisMonth)
                .appointmentsLastMonth(appointmentsLastMonth)
                .appointmentsTrend(appointmentsTrend)
                .globalCancellationRate(cancellationRate)
                .build();
    }

    // ==================== Qualité ====================

    @Override
    public AdminQualityStatsDTO getQualityStats() {
        long completedCount = appointmentRepository.countByStatus("COMPLETED");
        long confirmedCount = appointmentRepository.countByStatus("CONFIRMED");
        long cancelledCount = appointmentRepository.countByStatus("CANCELLED");
        long totalCount     = completedCount + confirmedCount + cancelledCount;

        double completedPct = totalCount > 0 ? Math.round((completedCount * 100.0 / totalCount) * 10.0) / 10.0 : 0;
        double confirmedPct = totalCount > 0 ? Math.round((confirmedCount * 100.0 / totalCount) * 10.0) / 10.0 : 0;
        double cancelledPct = totalCount > 0 ? Math.round((cancelledCount * 100.0 / totalCount) * 10.0) / 10.0 : 0;

        return AdminQualityStatsDTO.builder()
                .completedCount(completedCount)
                .confirmedCount(confirmedCount)
                .cancelledCount(cancelledCount)
                .totalCount(totalCount)
                .completedPercent(completedPct)
                .confirmedPercent(confirmedPct)
                .cancelledPercent(cancelledPct)
                .build();
    }

    // ==================== Demandes en attente ====================

    private AdminPendingRequestsDTO getPendingRequests() {
        long totalPending      = adminUserRepository.countTotalPending();
        long pendingMedecins   = adminUserRepository.countPendingByRole("MEDECIN");
        long pendingLabos      = adminUserRepository.countPendingByRole("LABORATOIRE");

        return AdminPendingRequestsDTO.builder()
                .totalPending(totalPending)
                .pendingMedecins(pendingMedecins)
                .pendingLaboratoires(pendingLabos)
                .build();
    }

    // ==================== Répartition Géographique ====================

    private static final String[] TUNISIAN_CITIES = {
        "Ariana", "Béja", "Ben Arous", "Bizerte", "Gabès", "Gafsa", "Jendouba", "Kairouan",
        "Kasserine", "Kébili", "Le Kef", "Mahdia", "La Manouba", "Médenine", "Monastir",
        "Nabeul", "Sfax", "Sidi Bouzid", "Siliana", "Sousse", "Tataouine", "Tozeur", "Tunis", "Zaghouan"
    };

    private String normalizeCityName(String ville) {
        if (ville == null || ville.trim().isEmpty()) return null;
        String normalized = ville.trim().toLowerCase()
                .replace("beja", "Béja").replace("gabes", "Gabès")
                .replace("kebili", "Kébili").replace("kef", "Le Kef")
                .replace("medenine", "Médenine");
        normalized = normalized.substring(0, 1).toUpperCase() + normalized.substring(1);
        
        // Match avec la liste officielle si possible
        for (String officialCity : TUNISIAN_CITIES) {
            if (officialCity.equalsIgnoreCase(normalized) || officialCity.equalsIgnoreCase(ville.trim())) {
                return officialCity;
            }
        }
        return normalized;
    }

    private List<AdminCityStatsDTO> getCitiesDistribution() {
        List<Object[]> medecinsData = medecinRepository.countMedecinsByVille();
        List<Object[]> labosData = laboratoireRepository.countLabosByVille();

        Map<String, AdminCityStatsDTO> cityMap = new HashMap<>();
        
        // 1. Initialiser avec les 24 villes à 0
        for (String city : TUNISIAN_CITIES) {
            cityMap.put(city, new AdminCityStatsDTO(city, 0, 0));
        }

        // 2. Peupler avec les données Médecins
        for (Object[] row : medecinsData) {
            String ville = normalizeCityName((String) row[0]);
            long count = ((Number) row[1]).longValue();
            if (ville != null) {
                cityMap.putIfAbsent(ville, new AdminCityStatsDTO(ville, 0, 0));
                cityMap.get(ville).setMedecinsCount(cityMap.get(ville).getMedecinsCount() + count);
            }
        }

        // 3. Peupler avec les données Labos
        for (Object[] row : labosData) {
            String ville = normalizeCityName((String) row[0]);
            long count = ((Number) row[1]).longValue();
            if (ville != null) {
                cityMap.putIfAbsent(ville, new AdminCityStatsDTO(ville, 0, 0));
                cityMap.get(ville).setLabosCount(cityMap.get(ville).getLabosCount() + count);
            }
        }

        List<AdminCityStatsDTO> result = new ArrayList<>(cityMap.values());
        // Trier par total décroissant (Médecins + Labos), puis par ordre alphabétique
        result.sort((a, b) -> {
            long totalA = a.getMedecinsCount() + a.getLabosCount();
            long totalB = b.getMedecinsCount() + b.getLabosCount();
            if (totalA != totalB) {
                return Long.compare(totalB, totalA); // Ordre décroissant
            }
            return a.getVille().compareToIgnoreCase(b.getVille());
        });
        return result;
    }

    // ==================== Croissance ====================


    @Override
    public AdminGrowthDataDTO getGrowthData(int year, Integer month) {
        List<AdminTrendPoint> medecinAppointments;
        List<AdminTrendPoint> laboAppointments;

        if (month != null) {
            int daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();
            medecinAppointments = buildDailyTrend(
                    appointmentRepository.countMedecinAppointmentsByDay(year, month), daysInMonth);
            laboAppointments = buildDailyTrend(
                    appointmentRepository.countLaboAppointmentsByDay(year, month), daysInMonth);
        } else {
            medecinAppointments = buildMonthlyTrend(
                    appointmentRepository.countMedecinAppointmentsByMonth(year));
            laboAppointments = buildMonthlyTrend(
                    appointmentRepository.countLaboAppointmentsByMonth(year));
        }

        List<AdminTrendPoint> newPatients = buildMonthlyTrend(
                adminUserRepository.countNewUsersByRoleAndMonth("PATIENT", year));
        List<AdminTrendPoint> newMedecins = buildMonthlyTrend(
                adminUserRepository.countNewUsersByRoleAndMonth("MEDECIN", year));
        List<AdminTrendPoint> newLabos = buildMonthlyTrend(
                adminUserRepository.countNewUsersByRoleAndMonth("LABORATOIRE", year));

        return AdminGrowthDataDTO.builder()
                .medecinAppointments(medecinAppointments)
                .laboAppointments(laboAppointments)
                .newPatients(newPatients)
                .newMedecins(newMedecins)
                .newLabos(newLabos)
                .build();
    }

    // ==================== Helpers ====================

    private List<AdminTrendPoint> buildMonthlyTrend(List<Object[]> rows) {
        Map<Integer, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            int monthNum = ((Number) row[0]).intValue();
            long count   = ((Number) row[1]).longValue();
            map.put(monthNum, count);
        }
        List<AdminTrendPoint> result = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            result.add(new AdminTrendPoint(MONTH_LABELS[i - 1], map.getOrDefault(i, 0L)));
        }
        return result;
    }

    private List<AdminTrendPoint> buildDailyTrend(List<Object[]> rows, int daysInMonth) {
        Map<Integer, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            int day  = ((Number) row[0]).intValue();
            long cnt = ((Number) row[1]).longValue();
            map.put(day, cnt);
        }
        List<AdminTrendPoint> result = new ArrayList<>();
        for (int i = 1; i <= daysInMonth; i++) {
            result.add(new AdminTrendPoint(String.valueOf(i), map.getOrDefault(i, 0L)));
        }
        return result;
    }
}
