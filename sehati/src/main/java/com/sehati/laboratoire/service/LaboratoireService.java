package com.sehati.laboratoire.service;

import com.sehati.auth.dto.SignupLaboRequest;
import com.sehati.auth.entities.User;
import com.sehati.laboratoire.dto.LaboProfileDTO;
import com.sehati.laboratoire.dto.LaboHoraireDTO;
import com.sehati.common.dto.WorkHoursDTO;
import com.sehati.common.dto.DayScheduleDTO;
import com.sehati.common.entities.WorkHours;
import com.sehati.common.entities.DaySchedule;
import com.sehati.laboratoire.entities.Laboratoire;
import com.sehati.laboratoire.repository.LaboratoireRepository;
import com.sehati.common.entities.PhoneNumber;
import com.sehati.common.service.CloudinaryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LaboratoireService {

    private final LaboratoireRepository laboratoireRepository;

    public LaboratoireService(LaboratoireRepository laboratoireRepository) {
        this.laboratoireRepository = laboratoireRepository;
    }

    @Transactional
    public void createLaboratoireProfile(User savedUser, SignupLaboRequest request) {
        Laboratoire labo = new Laboratoire();
        labo.setNomLabo(request.getNomLabo());
        labo.setAdresseComplete(request.getAdresseComplete());
        labo.setVille(request.getVille());
        
        if (request.getPhones() != null) {
            java.util.List<PhoneNumber> phoneNumbers = new java.util.ArrayList<>();
            for (String p : request.getPhones()) {
                PhoneNumber pn = new PhoneNumber(p);
                pn.setLaboratoire(labo);
                phoneNumbers.add(pn);
            }
            labo.setPhones(phoneNumbers);
        }
        
        labo.setAnalyses(request.getAnalyses());
        labo.setRegistreCommerceUrl(request.getRegistreCommerceUrl());
        labo.setLatitude(request.getLatitude());
        labo.setLongitude(request.getLongitude());
        labo.setUser(savedUser);
        laboratoireRepository.save(labo);
    }

    public boolean exists(Long id) {
        return laboratoireRepository.existsById(id);
    }

    // ==================== Profile Management ====================

    public LaboProfileDTO getLaboProfile(Long userId) {
        Laboratoire labo = laboratoireRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Laboratoire non trouvé"));
        return toProfileDTO(labo);
    }

    @Transactional
    public LaboProfileDTO updateLaboProfile(Long userId, LaboProfileDTO dto) {
        Laboratoire labo = laboratoireRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Laboratoire non trouvé"));

        if (dto.getNomLabo() != null) labo.setNomLabo(dto.getNomLabo());
        if (dto.getResponsable() != null) labo.setResponsable(dto.getResponsable());

        // Update Coordonnées
        if (dto.getAdresseComplete() != null) labo.setAdresseComplete(dto.getAdresseComplete());
        if (dto.getVille() != null) labo.setVille(dto.getVille());
        if (dto.getLatitude() != null) labo.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) labo.setLongitude(dto.getLongitude());

        if (dto.getPhones() != null) {
            labo.getPhones().clear();
            for (String p : dto.getPhones()) {
                PhoneNumber pn = new PhoneNumber(p);
                pn.setLaboratoire(labo);
                labo.getPhones().add(pn);
            }
        }

        // Update Analyses
        if (dto.getAnalyses() != null) {
            labo.setAnalyses(dto.getAnalyses());
        }

        laboratoireRepository.save(labo);
        return toProfileDTO(labo);
    }

    @Transactional
    public void updateLaboPhoto(Long userId, String newPhotoUrl, CloudinaryService cloudinaryService) {
        Laboratoire labo = laboratoireRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Laboratoire non trouvé"));
        String oldUrl = labo.getPhotoProfilUrl();
        if (oldUrl != null && !oldUrl.isBlank()) cloudinaryService.deleteFile(oldUrl);
        labo.setPhotoProfilUrl(newPhotoUrl);
        laboratoireRepository.save(labo);
    }

    private LaboProfileDTO toProfileDTO(Laboratoire labo) {
        String email = labo.getUser() != null ? labo.getUser().getEmail() : null;
        return LaboProfileDTO.builder()
                .nomLabo(labo.getNomLabo())
                .responsable(labo.getResponsable())
                .email(email)
                .photoProfilUrl(labo.getPhotoProfilUrl())
                .adresseComplete(labo.getAdresseComplete())
                .ville(labo.getVille())
                .latitude(labo.getLatitude())
                .longitude(labo.getLongitude())
                .phones(labo.getPhones() != null
                        ? labo.getPhones().stream().map(PhoneNumber::getNumber).collect(Collectors.toList())
                        : Collections.emptyList())
                .analyses(labo.getAnalyses() != null ? labo.getAnalyses() : Collections.emptyList())
                .build();
    }

    // ==================== Horaire d'Ouverture ====================

    public LaboHoraireDTO getHoraire(Long userId) {
        Laboratoire labo = laboratoireRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Laboratoire non trouvé"));
        
        return LaboHoraireDTO.builder()
                .consultationTime(labo.getConsultationTime())
                .capaciteParCreneau(labo.getCapaciteParCreneau())
                .workHours(mapWorkHoursToDTO(labo.getWorkHours()))
                .build();
    }

    @Transactional
    public LaboHoraireDTO updateHoraire(Long userId, LaboHoraireDTO dto) {
        Laboratoire labo = laboratoireRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Laboratoire non trouvé"));

        if (dto.getConsultationTime() != null) {
            labo.setConsultationTime(dto.getConsultationTime());
        }
        
        if (dto.getCapaciteParCreneau() != null) {
            labo.setCapaciteParCreneau(dto.getCapaciteParCreneau());
        }

        if (dto.getWorkHours() != null) {
            WorkHours wh = labo.getWorkHours();
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
            labo.setWorkHours(wh);
        }

        laboratoireRepository.save(labo);
        return LaboHoraireDTO.builder()
                .consultationTime(labo.getConsultationTime())
                .capaciteParCreneau(labo.getCapaciteParCreneau())
                .workHours(mapWorkHoursToDTO(labo.getWorkHours()))
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

    private DaySchedule mapDayFromDTO(DayScheduleDTO dto) {
        if (dto == null) return null;
        return DaySchedule.builder()
                .debut(dto.getDebut())
                .fin(dto.getFin())
                .ferme(dto.isFerme())
                .build();
    }
}
