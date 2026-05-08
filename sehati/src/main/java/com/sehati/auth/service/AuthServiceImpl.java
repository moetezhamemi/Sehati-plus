package com.sehati.auth.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import com.sehati.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.sehati.auth.dto.ForgotPasswordRequest;
import com.sehati.auth.dto.GoogleAuthRequest;
import com.sehati.auth.dto.JwtResponse;
import com.sehati.auth.dto.LoginRequest;
import com.sehati.auth.dto.PatientRegistrationResponse;
import com.sehati.auth.dto.ResetPasswordOtpRequest;
import com.sehati.auth.dto.SignupLaboRequest;
import com.sehati.auth.dto.SignupMedecinRequest;
import com.sehati.auth.dto.SignupPatientRequest;
import com.sehati.auth.entities.Role;
import com.sehati.auth.entities.User;
import com.sehati.auth.repositories.RoleRepository;
import com.sehati.auth.repositories.UserRepository;
import com.sehati.auth.security.JwtUtils;
import com.sehati.auth.security.UserDetailsImpl;
import com.sehati.common.service.TwilioSmsService;
import com.sehati.laboratoire.service.LaboratoireService;
import com.sehati.medecin.service.MedecinService;
import com.sehati.patient.entities.Patient;
import com.sehati.patient.repository.PatientRepository;
import com.sehati.patient.service.PatientService;

import jakarta.transaction.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PatientService patientService;
    private final PatientRepository patientRepository;
    private final MedecinService medecinService;
    private final LaboratoireService laboratoireService;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;
    private final OtpService otpService;
    private final TwilioSmsService twilioSmsService;
    private final StringRedisTemplate redisTemplate;
    private final com.sehati.secretaire.repository.MedecinSecretaireRepository medecinSecretaireRepository;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PatientService patientService,
            PatientRepository patientRepository,
            MedecinService medecinService,
            LaboratoireService laboratoireService,
            PasswordEncoder encoder,
            JwtUtils jwtUtils,
            EmailService emailService,
            OtpService otpService,
            TwilioSmsService twilioSmsService,
            StringRedisTemplate redisTemplate,
            com.sehati.secretaire.repository.MedecinSecretaireRepository medecinSecretaireRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.patientService = patientService;
        this.patientRepository = patientRepository;
        this.medecinService = medecinService;
        this.laboratoireService = laboratoireService;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.emailService = emailService;
        this.otpService = otpService;
        this.twilioSmsService = twilioSmsService;
        this.redisTemplate = redisTemplate;
        this.medecinSecretaireRepository = medecinSecretaireRepository;
    }

    @Override
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        if (!userDetails.getIsEmailVerified()) {
            throw new BusinessException("Error: Email is not verified.");
        }

        if (!userDetails.isEnabled()) {
            throw new BusinessException("Error: Account is pending admin approval.");
        }

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new JwtResponse(jwt, userDetails.getId(), userDetails.getEmail(), roles);
    }

    private Role getRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName(roleName);
                    return roleRepository.save(r);
                });
    }

    private void checkEmailAndPasswords(String email, String password, String confirmPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Error: Email is already in use!");
        }
        if (!password.equals(confirmPassword)) {
            throw new BusinessException("Error: Passwords do not match!");
        }
    }

    private User createUser(String email, String password, String roleName, boolean enabled, String status) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(encoder.encode(password));
        List<Role> roles = new ArrayList<>();
        roles.add(getRole(roleName));
        user.setRoles(roles);
        user.setEnabled(enabled);
        user.setStatus(status);
        user.setIsEmailVerified(false);
        return user;
    }

    private User saveUserAndSendOtp(User user) {
        User savedUser = userRepository.save(user);
        try {
            String otp = otpService.generateAndStore(savedUser.getEmail(), "EMAIL_VERIFICATION");
            emailService.sendVerificationEmail(savedUser.getEmail(), otp);
            logger.info("Verification OTP email sent to {}", savedUser.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send verification email to {}", savedUser.getEmail(), e);
            throw new BusinessException("Erreur lors de l'envoi de l'email de vérification. " + e.getMessage());
        }
        return savedUser;
    }

    @Override
    @Transactional
    public PatientRegistrationResponse registerPatient(SignupPatientRequest signUpRequest) {
        checkEmailAndPasswords(signUpRequest.getEmail(), signUpRequest.getPassword(),
                signUpRequest.getConfirmPassword());

        // Vérifier si un patient existe déjà avec ce numéro de téléphone
        Optional<Patient> existingPatient = patientRepository.findFirstByTelephone(signUpRequest.getTelephone());
        boolean requiresPhoneVerification = false;

        if (existingPatient.isPresent() && existingPatient.get().getUser() != null) {
            // Cas 3 : Patient avec un compte User existant → bloquer
            throw new BusinessException("Ce numéro de téléphone est déjà associé à un compte existant.");
        }

        User user = createUser(signUpRequest.getEmail(), signUpRequest.getPassword(), "PATIENT", true, "APPROVED");
        User savedUser = saveUserAndSendOtp(user);

        if (existingPatient.isPresent()) {
            // Cas 2 : Patient existant sans User → fusion après vérification SMS
            requiresPhoneVerification = true;

            // Stocker dans Redis la paire email→patientId pour la fusion post-vérification
            redisTemplate.opsForValue().set(
                    "phone_merge:" + savedUser.getEmail(),
                    existingPatient.get().getId().toString(),
                    Duration.ofHours(1));

            // Stocker aussi les infos du signup pour mettre à jour le patient après fusion
            redisTemplate.opsForValue().set(
                    "phone_merge_data:" + savedUser.getEmail(),
                    signUpRequest.getNom() + "|" + signUpRequest.getPrenom() + "|" + signUpRequest.getDateNaissance(),
                    Duration.ofHours(1));

            logger.info("Registered new PATIENT user: {} — requires phone verification (existing patient ID: {})",
                    savedUser.getEmail(), existingPatient.get().getId());
        } else {
            // Cas 1 : Pas de patient existant → inscription normale
            patientService.createPatientOrchestrator(savedUser, signUpRequest);
            logger.info("Registered new PATIENT user: {}", savedUser.getEmail());
        }

        return PatientRegistrationResponse.builder()
                .message("Un email de vérification vous a été envoyé, veuillez confirmer votre email.")
                .requiresPhoneVerification(requiresPhoneVerification)
                .email(signUpRequest.getEmail())
                .build();
    }

    @Override
    @Transactional
    public String registerMedecin(SignupMedecinRequest signUpRequest) {
        checkEmailAndPasswords(signUpRequest.getEmail(), signUpRequest.getPassword(),
                signUpRequest.getConfirmPassword());

        User user = createUser(signUpRequest.getEmail(), signUpRequest.getPassword(), "MEDECIN", false, "PENDING");
        User savedUser = saveUserAndSendOtp(user);

        medecinService.createMedecinProfile(savedUser, signUpRequest);

        logger.info("Registered new MEDECIN user (Pending): {}", savedUser.getEmail());

        return "Un email de validation a été envoyé, veuillez confirmer votre email.";
    }

    @Override
    @Transactional
    public String registerLaboratoire(SignupLaboRequest signUpRequest) {
        checkEmailAndPasswords(signUpRequest.getEmail(), signUpRequest.getPassword(),
                signUpRequest.getConfirmPassword());

        User user = createUser(signUpRequest.getEmail(), signUpRequest.getPassword(), "LABORATOIRE", false, "PENDING");
        User savedUser = saveUserAndSendOtp(user);

        laboratoireService.createLaboratoireProfile(savedUser, signUpRequest);

        logger.info("Registered new LABORATOIRE user (Pending): {}", savedUser.getEmail());

        return "Un email de validation a été envoyé, veuillez confirmer votre email.";
    }

    @Override
    @Transactional
    public String sendOtp(String email, String purpose) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Aucun compte trouvé avec cet email."));

        if ("EMAIL_VERIFICATION".equals(purpose) && Boolean.TRUE.equals(user.getIsEmailVerified())) {
            throw new BusinessException("Cet email est déjà vérifié.");
        }

        try {
            String otp = otpService.generateAndStore(email, purpose);
            if ("PASSWORD_RESET".equals(purpose)) {
                emailService.sendPasswordResetEmail(email, otp);
            } else {
                emailService.sendVerificationEmail(email, otp);
            }
            logger.info("Sent OTP for {} to {}", purpose, email);
            return "Un email avec le code a été envoyé.";
        } catch (Exception e) {
            logger.error("Failed to send OTP to {}", email, e);
            throw new BusinessException(e.getMessage() != null ? e.getMessage() : "Erreur lors de l'envoi de l'email.");
        }
    }

    @Override
    @Transactional
    public java.util.Map<String, Object> verifyEmailOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Aucun compte trouvé avec cet email."));

        OtpValidationResult result = otpService.validateAndInvalidate(email, "EMAIL_VERIFICATION", otp);
        if (result == OtpValidationResult.EXPIRED) {
            throw new BusinessException("Ce code est expiré. Veuillez demander un nouveau code.");
        } else if (result == OtpValidationResult.MAX_ATTEMPTS_REACHED) {
            throw new BusinessException("Trop de tentatives. Veuillez demander un nouveau code.");
        } else if (result == OtpValidationResult.INVALID) {
            throw new BusinessException("Code incorrect.");
        }

        user.setIsEmailVerified(true);
        userRepository.save(user);

        String role = user.getRoles().isEmpty() ? "" : user.getRoles().get(0).getName();
        String status = user.getStatus();

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("message", "Votre email a été vérifié avec succès.");
        response.put("role", role);
        response.put("status", status);

        // Vérifier si une fusion SMS est requise
        String mergeKey = "phone_merge:" + email;
        boolean requiresPhoneVerification = Boolean.TRUE.equals(redisTemplate.hasKey(mergeKey));
        response.put("requiresPhoneVerification", requiresPhoneVerification);

        // If the user is already approved (e.g., Patients) AND no phone verification needed,
        // generate a token for automatic login
        if ("APPROVED".equals(status) && !requiresPhoneVerification) {
            UserDetailsImpl userDetails = UserDetailsImpl.build(user);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,
                    null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtUtils.generateJwtToken(authentication);
            response.put("token", jwt);
            response.put("id", userDetails.getId());
            response.put("email", userDetails.getEmail());

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            response.put("roles", roles);

            logger.info("Auto-login generated for user: {}, roles: {}", email, roles);
        }

        return response;
    }

    // =========================================================
    // SMS OTP — Liaison dossier médical existant
    // =========================================================

    @Override
    @Transactional
    public String sendSmsOtp(String telephone, String email) {
        // Vérifier que le contexte de fusion existe dans Redis
        String mergeKey = "phone_merge:" + email;
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(mergeKey))) {
            throw new BusinessException("Aucune demande de vérification en cours pour cet email.");
        }

        try {
            twilioSmsService.sendVerificationCode(telephone);
            logger.info("SMS verification sent for phone merge: email={}, phone={}", email, telephone);
            return "Un code de vérification a été envoyé par SMS.";
        } catch (Exception e) {
            logger.error("Failed to send SMS to {}", telephone, e);
            throw new BusinessException("Erreur lors de l'envoi du SMS. Veuillez réessayer.");
        }
    }

    @Override
    @Transactional
    public java.util.Map<String, Object> verifySmsOtp(String telephone, String code, String email) {
        // Vérifier le code via Twilio
        boolean approved;
        try {
            approved = twilioSmsService.verifyCode(telephone, code);
        } catch (Exception e) {
            logger.error("Twilio verification failed for phone {}", telephone, e);
            throw new BusinessException("Erreur lors de la vérification du code SMS.");
        }

        if (!approved) {
            throw new BusinessException("Code SMS incorrect ou expiré.");
        }

        // Récupérer le patientId depuis Redis
        String mergeKey = "phone_merge:" + email;
        String patientIdStr = redisTemplate.opsForValue().get(mergeKey);
        if (patientIdStr == null) {
            throw new BusinessException("La session de vérification a expiré. Veuillez recommencer l'inscription.");
        }

        Long patientId = Long.parseLong(patientIdStr);

        // Récupérer le user et le patient
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Aucun compte trouvé avec cet email."));
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new BusinessException("Dossier patient introuvable."));

        // Lier le User au Patient existant
        patient.setUser(user);

        // Mettre à jour les infos du patient avec les données du signup
        String mergeDataKey = "phone_merge_data:" + email;
        String mergeData = redisTemplate.opsForValue().get(mergeDataKey);
        if (mergeData != null) {
            String[] parts = mergeData.split("\\|", 3);
            if (parts.length >= 2) {
                patient.setNom(parts[0]);
                patient.setPrenom(parts[1]);
            }
            if (parts.length == 3 && !parts[2].equals("null")) {
                try {
                    patient.setDateNaissance(java.time.LocalDate.parse(parts[2]));
                } catch (Exception ignored) {
                    // keep existing dateNaissance if parse fails
                }
            }
        }

        patientRepository.save(patient);

        // Nettoyer Redis
        redisTemplate.delete(mergeKey);
        redisTemplate.delete(mergeDataKey);

        logger.info("Patient {} merged with user {} after SMS verification", patientId, user.getEmail());

        // Auto-login
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,
                null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("token", jwt);
        response.put("id", userDetails.getId());
        response.put("email", userDetails.getEmail());
        response.put("roles", roles);
        response.put("message", "Votre dossier médical a été lié avec succès à votre compte.");

        return response;
    }

    @Override
    @Transactional
    public String resendVerificationEmail(String email) {
        return sendOtp(email, "EMAIL_VERIFICATION");
    }

    @Override
    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user != null) {
            try {
                String otp = otpService.generateAndStore(user.getEmail(), "PASSWORD_RESET");
                emailService.sendPasswordResetEmail(user.getEmail(), otp);
                logger.info("Password reset OTP email sent to {}", user.getEmail());
            } catch (Exception e) {
                logger.error("Failed to send password reset OTP to {}", user.getEmail(), e);
                throw new BusinessException(e.getMessage());
            }
        } else {
            logger.warn("Password reset OTP requested for non-existing email: {}", request.getEmail());
        }
        return "Si cette adresse email est associée à un compte, un code avec des instructions vous sera envoyé.";
    }

    @Override
    @Transactional
    public String resetPassword(ResetPasswordOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Aucun compte trouvé."));

        OtpValidationResult result = otpService.validateAndInvalidate(request.getEmail(), "PASSWORD_RESET",
                request.getOtp());
        if (result == OtpValidationResult.EXPIRED) {
            throw new BusinessException("Ce code est expiré. Veuillez demander une nouvelle réinitialisation.");
        } else if (result == OtpValidationResult.MAX_ATTEMPTS_REACHED) {
            throw new BusinessException("Trop de tentatives. Veuillez demander une nouvelle réinitialisation.");
        } else if (result == OtpValidationResult.INVALID) {
            throw new BusinessException("Code incorrect.");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Les mots de passe ne correspondent pas.");
        }

        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return "Votre mot de passe a été modifié avec succès. Vous pouvez maintenant vous connecter.";
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public JwtResponse loginWithGoogle(GoogleAuthRequest request) {
        String googleTokenInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + request.getIdToken();
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> googlePayload;
        try {
            googlePayload = restTemplate.getForObject(googleTokenInfoUrl, Map.class);
        } catch (Exception e) {
            logger.error("Failed to validate Google token", e);
            throw new BusinessException("Token Google invalide ou expiré. Veuillez réessayer.");
        }

        if (googlePayload == null || googlePayload.containsKey("error_description")) {
            throw new BusinessException("Token Google invalide. Veuillez réessayer.");
        }

        String email = (String) googlePayload.get("email");

        if (email == null || email.isBlank()) {
            throw new BusinessException("Impossible de récupérer l'email depuis le compte Google.");
        }

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = createUser(email, UUID.randomUUID().toString(), "PATIENT", true, "APPROVED");
            newUser.setIsEmailVerified(true);
            newUser.setVerificationToken(null);
            User savedUser = userRepository.save(newUser);

            patientService.createPatientFromGoogle(
                    savedUser,
                    (String) googlePayload.getOrDefault("given_name", ""),
                    (String) googlePayload.getOrDefault("family_name", ""));

            logger.info("Created new patient from Google login: {}", email);

            return savedUser;
        });

        // 3. Load user details and generate Sehati JWT
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);
        return new JwtResponse(jwt, userDetails.getId(), userDetails.getEmail(), roles);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé."));

        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("Le mot de passe actuel est incorrect.");
        }

        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Password updated for user id: {}", userId);
    }

    @Override
    @Transactional
    public com.sehati.auth.dto.SecretaireTokenCheckResponse checkSecretaireToken(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new BusinessException("Jeton d'invitation invalide ou expiré."));

        boolean hasPassword = user.getPassword() != null && !user.getPassword().isEmpty();

        if (!hasPassword) {
            return com.sehati.auth.dto.SecretaireTokenCheckResponse.builder().needsSetup(true).build();
        }

        // Le compte a déjà un mot de passe -> auto login
        user.setVerificationToken(null);
        userRepository.save(user);

        // Activer la relation MedecinSecretaire PENDING → ACTIVE
        medecinSecretaireRepository.findBySecretaireUserIdAndStatus(user.getId(), "PENDING")
                .ifPresent(relation -> {
                    relation.setStatus("ACTIVE");
                    medecinSecretaireRepository.save(relation);
                    logger.info("Relation MedecinSecretaire activée via auto-login pour la secrétaire user ID: {}",
                            user.getId());
                });

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        logger.info("Auto-login generated for existing secretaire via token: {}", user.getEmail());

        JwtResponse jwtResponse = new JwtResponse(jwt, userDetails.getId(), user.getEmail(), roles);
        return com.sehati.auth.dto.SecretaireTokenCheckResponse.builder()
                .needsSetup(false)
                .authData(jwtResponse)
                .build();
    }

    @Override
    @Transactional
    public JwtResponse setupSecretairePassword(com.sehati.auth.dto.SecretaireSetupPasswordRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Les mots de passe ne correspondent pas.");
        }

        User user = userRepository.findByVerificationToken(request.getToken())
                .orElseThrow(() -> new BusinessException("Jeton d'invitation invalide ou expiré."));

        user.setPassword(encoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setStatus("APPROVED");
        user.setIsEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        // Activer la relation MedecinSecretaire PENDING → ACTIVE
        medecinSecretaireRepository.findBySecretaireUserIdAndStatus(user.getId(), "PENDING")
                .ifPresent(relation -> {
                    relation.setStatus("ACTIVE");
                    medecinSecretaireRepository.save(relation);
                    logger.info("Relation MedecinSecretaire activée pour la secrétaire user ID: {}", user.getId());
                });

        // Auto login
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        logger.info("Auto-login generated for secretaire setup: {}, roles: {}", user.getEmail(), roles);

        return new JwtResponse(jwt, userDetails.getId(), user.getEmail(), roles);
    }
}
