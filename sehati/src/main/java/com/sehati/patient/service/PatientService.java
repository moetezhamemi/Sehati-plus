package com.sehati.patient.service;


import com.sehati.appointment.entities.Appointment;
import com.sehati.appointment.repository.AppointmentRepository;
import com.sehati.auth.dto.SignupPatientRequest;
import com.sehati.auth.entities.User;
import com.sehati.auth.repositories.UserRepository;
import com.sehati.patient.entities.Patient;
import com.sehati.patient.repository.PatientRepository;
import com.sehati.medecin.repository.MedecinRepository;
import com.sehati.laboratoire.repository.LaboratoireRepository;
import com.sehati.secretaire.repository.SecretaireRepository;
import com.sehati.secretaire.repository.MedecinSecretaireRepository;
import com.sehati.patient.dto.CreatePatientDTO;
import com.sehati.patient.dto.ConsultationItemDTO;
import com.sehati.patient.dto.PatientHistoryDTO;
import com.sehati.patient.dto.PatientProfileDTO;
import com.sehati.patient.dto.ProfessionalPatientDTO;
import com.sehati.common.service.CloudinaryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
public class PatientService {

    private final PatientRepository patientRepository;
    private final MedecinRepository medecinRepository;
    private final LaboratoireRepository laboratoireRepository;
    private final SecretaireRepository secretaireRepository;
    private final MedecinSecretaireRepository medecinSecretaireRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final CloudinaryService cloudinaryService;

    public PatientService(PatientRepository patientRepository,
            MedecinRepository medecinRepository,
            LaboratoireRepository laboratoireRepository,
            SecretaireRepository secretaireRepository,
            MedecinSecretaireRepository medecinSecretaireRepository,
            UserRepository userRepository,
            AppointmentRepository appointmentRepository,
            CloudinaryService cloudinaryService) {
        this.patientRepository = patientRepository;
        this.medecinRepository = medecinRepository;
        this.laboratoireRepository = laboratoireRepository;
        this.secretaireRepository = secretaireRepository;
        this.medecinSecretaireRepository = medecinSecretaireRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional
    public void createPatientOrchestrator(User savedUser, SignupPatientRequest request) {
        Patient patient = new Patient();
        patient.setNom(request.getNom());
        patient.setPrenom(request.getPrenom());
        patient.setTelephone(request.getTelephone());
        patient.setDateNaissance(request.getDateNaissance());
        patient.setUser(savedUser);
        patientRepository.save(patient);
    }

    @Transactional
    public void createPatientFromGoogle(User savedUser, String prenom, String nom) {
        Patient patient = new Patient();
        patient.setPrenom(prenom);
        patient.setNom(nom);
        patient.setUser(savedUser);
        patientRepository.save(patient);
    }

    public List<ProfessionalPatientDTO> getMyPatients(Long userId, String role, String search) {
        List<Patient> patients;

        if ("MEDECIN".equals(role)) {
            Long medecinId = medecinRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Médecin introuvable")).getId();
            patients = patientRepository.findDistinctPatientsByMedecinId(medecinId, search);
        } else if ("SECRETAIRE".equals(role)) {
            Long medecinId = secretaireRepository.findByUserId(userId)
                    .map(s -> medecinSecretaireRepository
                            .findBySecretaireUserIdAndStatus(s.getUser().getId(), "ACTIVE")
                            .orElseThrow(() -> new RuntimeException("Aucune relation active pour cette secrétaire"))
                            .getMedecin().getId())
                    .orElseThrow(() -> new RuntimeException("Secrétaire introuvable"));
            patients = patientRepository.findDistinctPatientsByMedecinId(medecinId, search);
        } else if ("LABORATOIRE".equals(role)) {
            Long laboId = laboratoireRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Laboratoire introuvable")).getId();
            patients = patientRepository.findDistinctPatientsByLaboratoireId(laboId, search);
        } else {
            throw new RuntimeException("Role non autorisé");
        }

        return patients.stream().map(this::toPatientDTO).collect(Collectors.toList());
    }

    public PatientHistoryDTO getPatientHistoryForMedecin(Long patientId, Long userId,
            String role) {
        Long medecinId = getMedecinIdForUser(userId, role);
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient introuvable"));

        List<Appointment> apps = appointmentRepository
                .findByPatientIdAndMedecinIdAndStatusAndDeletedByMedecinFalseOrderByDateDescTimeDesc(patientId,
                        medecinId, "COMPLETED");

        Integer age = null;
        if (patient.getDateNaissance() != null) {
            age = Period.between(patient.getDateNaissance(), LocalDate.now()).getYears();
        }

        LocalDate lastVisit = apps.isEmpty() ? null : apps.get(0).getDate();

        List<ConsultationItemDTO> consultationItems = apps.stream()
                .map(a -> ConsultationItemDTO.builder()
                        .id(a.getId())
                        .date(a.getDate())
                        .time(a.getTime())
                        .consultationNotes(a.getConsultationNotes())
                        .ordonnanceUrl(a.getOrdonnanceUrl())
                        .demandeAnalyseUrl(a.getDemandeAnalyseUrl())
                        .build())
                .collect(Collectors.toList());

        return PatientHistoryDTO.builder()
                .patientId(patient.getId())
                .nom(patient.getNom())
                .prenom(patient.getPrenom())
                .telephone(patient.getTelephone())
                .email(patient.getUser() != null ? patient.getUser().getEmail() : null)
                .age(age)
                .pdpUrl(patient.getPhotoProfilUrl())
                .derniereVisite(lastVisit)
                .consultations(consultationItems)
                .build();
    }

    @Transactional
    public void removePatientFromMedecinWorkspace(Long patientId, Long userId, String role) {
        Long medecinId = getMedecinIdForUser(userId, role);
        List<Appointment> apps = appointmentRepository
                .findByPatientIdAndMedecinId(patientId, medecinId);

        for (Appointment app : apps) {
            app.setDeletedByMedecin(true);
        }
        appointmentRepository.saveAll(apps);
    }

    private Long getMedecinIdForUser(Long userId, String role) {
        if ("MEDECIN".equals(role)) {
            return medecinRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Médecin introuvable")).getId();
        } else if ("SECRETAIRE".equals(role)) {
            return secretaireRepository.findByUserId(userId)
                    .map(s -> medecinSecretaireRepository
                            .findBySecretaireUserIdAndStatus(s.getUser().getId(), "ACTIVE")
                            .orElseThrow(() -> new RuntimeException("Aucune relation active pour cette secrétaire"))
                            .getMedecin().getId())
                    .orElseThrow(() -> new RuntimeException("Secrétaire introuvable"));
        } else {
            throw new RuntimeException("Rôle non autorisé pour consulter l'historique");
        }
    }

    public PatientProfileDTO getPatientProfile(Long userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Patient non trouvé"));
        String email = patient.getUser() != null ? patient.getUser().getEmail() : null;
        return PatientProfileDTO.builder()
                .nom(patient.getNom())
                .prenom(patient.getPrenom())
                .telephone(patient.getTelephone())
                .dateNaissance(patient.getDateNaissance())
                .email(email)
                .photoProfilUrl(patient.getPhotoProfilUrl())
                .build();
    }

    @Transactional
    public PatientProfileDTO updatePatientProfile(Long userId,
            PatientProfileDTO dto) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Patient non trouvé"));

        if (dto.getNom() != null)
            patient.setNom(dto.getNom());
        if (dto.getPrenom() != null)
            patient.setPrenom(dto.getPrenom());
        if (dto.getTelephone() != null)
            patient.setTelephone(dto.getTelephone());
        if (dto.getDateNaissance() != null)
            patient.setDateNaissance(dto.getDateNaissance());
        if (dto.getPhotoProfilUrl() != null)
            patient.setPhotoProfilUrl(dto.getPhotoProfilUrl());

        if (dto.getEmail() != null && patient.getUser() != null) {
            User user = patient.getUser();
            user.setEmail(dto.getEmail());
            userRepository.save(user);
        }

        patientRepository.save(patient);

        String email = patient.getUser() != null ? patient.getUser().getEmail() : null;
        return com.sehati.patient.dto.PatientProfileDTO.builder()
                .nom(patient.getNom())
                .prenom(patient.getPrenom())
                .telephone(patient.getTelephone())
                .dateNaissance(patient.getDateNaissance())
                .email(email)
                .photoProfilUrl(patient.getPhotoProfilUrl())
                .build();
    }

    @Transactional
    public void updatePatientPhoto(Long userId, String newPhotoUrl) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Patient non trouvé"));

        String oldPhotoUrl = patient.getPhotoProfilUrl();
        if (oldPhotoUrl != null && !oldPhotoUrl.isBlank()) {
            cloudinaryService.deleteFile(oldPhotoUrl);
        }

        patient.setPhotoProfilUrl(newPhotoUrl);
        patientRepository.save(patient);
    }
    // =========================================================
    // Recherche & Création manuelle de patient
    // =========================================================

    public java.util.Optional<ProfessionalPatientDTO> searchByTelephone(String telephone) {
        return patientRepository.findFirstByTelephone(telephone).map(this::toPatientDTO);
    }

    public List<ProfessionalPatientDTO> searchByFullName(String query) {
        return patientRepository.searchByFullName(query).stream()
                .map(this::toPatientDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProfessionalPatientDTO createPatientManually(CreatePatientDTO dto) {
        // Vérifier unicité du téléphone
        if (patientRepository.findFirstByTelephone(dto.getTelephone()).isPresent()) {
            throw new RuntimeException("Un patient avec ce numéro de téléphone existe déjà");
        }

        Patient patient = new Patient();
        patient.setNom(dto.getNom());
        patient.setPrenom(dto.getPrenom());
        patient.setDateNaissance(dto.getDateNaissance());
        patient.setTelephone(dto.getTelephone());
        // user reste null — patient sans compte
        patient = patientRepository.save(patient);
        return toPatientDTO(patient);
    }

    private ProfessionalPatientDTO toPatientDTO(Patient p) {
        return ProfessionalPatientDTO.builder()
                .id(p.getId())
                .nom(p.getNom())
                .prenom(p.getPrenom())
                .telephone(p.getTelephone())
                .photoProfilUrl(p.getPhotoProfilUrl())
                .dateNaissance(p.getDateNaissance())
                .build();
    }
}