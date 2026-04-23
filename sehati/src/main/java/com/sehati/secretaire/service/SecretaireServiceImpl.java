package com.sehati.secretaire.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sehati.auth.entities.Role;
import com.sehati.auth.entities.User;
import com.sehati.auth.repositories.RoleRepository;
import com.sehati.auth.repositories.UserRepository;
import com.sehati.auth.service.EmailService;
import com.sehati.common.exception.BusinessException;
import com.sehati.medecin.entities.Medecin;
import com.sehati.medecin.repository.MedecinRepository;
import com.sehati.secretaire.dto.AddSecretaireRequest;
import com.sehati.secretaire.dto.SecretaireResponse;
import com.sehati.secretaire.dto.SecretaireStatusResponse;
import com.sehati.secretaire.entities.MedecinSecretaire;
import com.sehati.secretaire.entities.Secretaire;
import com.sehati.secretaire.repository.MedecinSecretaireRepository;
import com.sehati.secretaire.repository.SecretaireRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecretaireServiceImpl implements SecretaireService {

    private final SecretaireRepository secretaireRepository;
    private final MedecinSecretaireRepository medecinSecretaireRepository;
    private final MedecinRepository medecinRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;

    private static final List<String> ACTIVE_STATUSES = List.of("PENDING", "ACTIVE");

    @Override
    public SecretaireResponse getMedecinSecretaire(Long userId) {
        Medecin medecin = medecinRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Médecin non trouvé."));

        Optional<MedecinSecretaire> optRelation =
                medecinSecretaireRepository.findByMedecinIdAndStatusIn(medecin.getId(), ACTIVE_STATUSES);

        if (optRelation.isEmpty()) {
            return null;
        }

        MedecinSecretaire relation = optRelation.get();
        return mapToResponse(relation.getSecretaire(), relation.getStatus());
    }

    @Override
    @Transactional
    public SecretaireResponse inviteSecretaire(Long userId, AddSecretaireRequest request) {
        Medecin medecin = medecinRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Médecin non trouvé."));

        // Vérifier que le médecin n'a pas déjà une secrétaire PENDING ou ACTIVE
        if (medecinSecretaireRepository.existsByMedecinIdAndStatusIn(medecin.getId(), ACTIVE_STATUSES)) {
            throw new BusinessException("Vous avez déjà une secrétaire associée à votre compte.");
        }

        // Récupérer ou créer le User et l'entité Secretaire
        Secretaire secretaire;
        String invitationToken;
        boolean alreadyHasPassword; // true = compte existant avec MDP → pas besoin de setup

        Optional<Secretaire> existingSecretaire = secretaireRepository.findByUserEmail(request.getEmail());

        if (existingSecretaire.isPresent()) {
            // Le compte existe déjà — réutiliser (ex: secrétaire invitée par un autre médecin)
            secretaire = existingSecretaire.get();
            // Vérifier que cette secrétaire n'est pas déjà active chez un autre médecin
            boolean alreadyActive = medecinSecretaireRepository
                    .findBySecretaireUserIdAndStatus(secretaire.getUser().getId(), "ACTIVE")
                    .isPresent();
            if (alreadyActive) {
                throw new BusinessException("Cette secrétaire est déjà active chez un autre médecin.");
            }
            // Mettre à jour le nom/prénom
            secretaire.setNom(request.getNom());
            secretaire.setPrenom(request.getPrenom());

            alreadyHasPassword = secretaire.getUser().getPassword() != null
                    && !secretaire.getUser().getPassword().isEmpty();

            invitationToken = UUID.randomUUID().toString();
            secretaire.getUser().setVerificationToken(invitationToken);

            if (alreadyHasPassword) {
                // Le compte a déjà un MDP — on active directement sans désactiver le compte
                // Le token sert juste à l'auto-login depuis le lien email
                secretaire.getUser().setEnabled(true);
            } else {
                // Nouvelle invitation — le compte attend la configuration du MDP
                secretaire.getUser().setEnabled(false);
            }

            userRepository.save(secretaire.getUser());
            secretaireRepository.save(secretaire);
        } else {
            // Nouveau compte secrétaire
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("Cet email appartient à un utilisateur existant avec un autre rôle.");
            }

            invitationToken = UUID.randomUUID().toString();
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword("");
            user.setEnabled(false);
            user.setStatus("APPROVED");
            user.setIsEmailVerified(false);
            user.setVerificationToken(invitationToken);

            Role secretaireRole = roleRepository.findByName("SECRETAIRE")
                    .orElseGet(() -> {
                        Role r = new Role();
                        r.setName("SECRETAIRE");
                        return roleRepository.save(r);
                    });
            user.setRoles(Collections.singletonList(secretaireRole));
            user = userRepository.save(user);

            secretaire = new Secretaire();
            secretaire.setNom(request.getNom());
            secretaire.setPrenom(request.getPrenom());
            secretaire.setUser(user);
            secretaire = secretaireRepository.save(secretaire);
            alreadyHasPassword = false; // Nouveau compte, toujours besoin du setup
        }

        // Si le compte a déjà un MDP → relation ACTIVE directement, sinon PENDING
        String initialStatus = alreadyHasPassword ? "ACTIVE" : "PENDING";

        MedecinSecretaire relation = MedecinSecretaire.builder()
                .medecin(medecin)
                .secretaire(secretaire)
                .status(initialStatus)
                .build();
        medecinSecretaireRepository.save(relation);

        // Envoyer l'email d'invitation
        String medecinName = medecin.getNom() + " " + medecin.getPrenom();
        emailService.sendSecretaireInvitationEmail(request.getEmail(), medecinName, invitationToken);

        log.info("Invitation envoyée à {} (secretaire ID: {}) par le médecin {}", request.getEmail(), secretaire.getId(), medecin.getId());

        return mapToResponse(secretaire, "PENDING");
    }

    @Override
    @Transactional
    public void removeSecretaire(Long userId) {
        Medecin medecin = medecinRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Médecin non trouvé."));

        MedecinSecretaire relation = medecinSecretaireRepository
                .findByMedecinIdAndStatusIn(medecin.getId(), ACTIVE_STATUSES)
                .orElseThrow(() -> new BusinessException("Aucune secrétaire active trouvée pour ce médecin."));

        // Soft remove : juste changer le statut — User et Secretaire intacts
        relation.setStatus("REMOVED");
        medecinSecretaireRepository.save(relation);

        log.info("Secrétaire {} retirée par le médecin {} (relation ID: {})",
                relation.getSecretaire().getId(), medecin.getId(), relation.getId());
    }

    @Override
    public SecretaireStatusResponse getSecretaireStatus(Long userId) {
        List<MedecinSecretaire> relations = medecinSecretaireRepository.findBySecretaireUserId(userId);

        if (relations.isEmpty()) {
            return new SecretaireStatusResponse("NONE", null);
        }

        // Chercher si la secrétaire a une relation ACTIVE
        Optional<MedecinSecretaire> activeRelation = relations.stream()
                .filter(r -> "ACTIVE".equals(r.getStatus()))
                .findFirst();

        if (activeRelation.isPresent()) {
            Medecin medecin = activeRelation.get().getMedecin();
            String medecinNom = "Dr. " + medecin.getNom() + " " + medecin.getPrenom();
            return new SecretaireStatusResponse("ACTIVE", medecinNom);
        }

        // Chercher si la secrétaire a une relation PENDING
        Optional<MedecinSecretaire> pendingRelation = relations.stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .findFirst();

        if (pendingRelation.isPresent()) {
            return new SecretaireStatusResponse("PENDING", null);
        }

        // Toutes ses relations sont REMOVED
        return new SecretaireStatusResponse("REMOVED", null);
    }

    private SecretaireResponse mapToResponse(Secretaire secretaire, String relationStatus) {
        return new SecretaireResponse(
                secretaire.getId(),
                secretaire.getNom(),
                secretaire.getPrenom(),
                secretaire.getUser().getEmail(),
                secretaire.getUser().getEnabled(),
                relationStatus
        );
    }
}
