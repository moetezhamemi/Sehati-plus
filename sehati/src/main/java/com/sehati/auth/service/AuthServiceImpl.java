package com.sehati.auth.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.sehati.auth.dto.MessageResponse;
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
import com.sehati.laboratoire.entities.Laboratoire;
import com.sehati.laboratoire.repository.LaboratoireRepository;
import com.sehati.medecin.entities.Medecin;
import com.sehati.medecin.repository.MedecinRepository;
import com.sehati.patient.entities.Patient;
import com.sehati.patient.repository.PatientRepository;

import jakarta.transaction.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PatientRepository patientRepository;
    private final MedecinRepository medecinRepository;
    private final LaboratoireRepository laboratoireRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;
    private final OtpService otpService;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           PatientRepository patientRepository,
                           MedecinRepository medecinRepository,
                           LaboratoireRepository laboratoireRepository,
                           PasswordEncoder encoder,
                           JwtUtils jwtUtils,
                           EmailService emailService,
                           OtpService otpService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.patientRepository = patientRepository;
        this.medecinRepository = medecinRepository;
        this.laboratoireRepository = laboratoireRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.emailService = emailService;
        this.otpService = otpService;
    }

    @Override
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        if (!userDetails.getIsEmailVerified()) {
            throw new RuntimeException("Error: Email is not verified.");
        }

        if (!userDetails.isEnabled()) {
            throw new RuntimeException("Error: Account is pending admin approval.");
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
            throw new RuntimeException("Error: Email is already in use!");
        }
        if (!password.equals(confirmPassword)) {
            throw new RuntimeException("Error: Passwords do not match!");
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
            throw new RuntimeException("Erreur lors de l'envoi de l'email de vérification. " + e.getMessage());
        }
        return savedUser;
    }

    @Override
    @Transactional
    public MessageResponse registerPatient(SignupPatientRequest signUpRequest) {
        checkEmailAndPasswords(signUpRequest.getEmail(), signUpRequest.getPassword(), signUpRequest.getConfirmPassword());

        User user = createUser(signUpRequest.getEmail(), signUpRequest.getPassword(), "PATIENT", true, "APPROVED");
        User savedUser = saveUserAndSendOtp(user);

        Patient patient = new Patient();
        patient.setNom(signUpRequest.getNom());
        patient.setPrenom(signUpRequest.getPrenom());
        patient.setTelephone(signUpRequest.getTelephone());
        patient.setDateNaissance(signUpRequest.getDateNaissance());
        patient.setUser(savedUser);
        patientRepository.save(patient);

        logger.info("Registered new PATIENT user: {}", savedUser.getEmail());

        return new MessageResponse("Un email de vérification vous a été envoyé, veuillez confirmer votre email.");
    }

    @Override
    @Transactional
    public MessageResponse registerMedecin(SignupMedecinRequest signUpRequest) {
        checkEmailAndPasswords(signUpRequest.getEmail(), signUpRequest.getPassword(), signUpRequest.getConfirmPassword());

        User user = createUser(signUpRequest.getEmail(), signUpRequest.getPassword(), "MEDECIN", false, "PENDING");
        User savedUser = saveUserAndSendOtp(user);

        Medecin medecin = new Medecin();
        medecin.setNom(signUpRequest.getNom());
        medecin.setPrenom(signUpRequest.getPrenom());
        medecin.setSpecialite(signUpRequest.getSpecialite());
        medecin.setAdresseCabinet(signUpRequest.getAdresseCabinet());
        medecin.setVille(signUpRequest.getVille());
        medecin.setTelephone(signUpRequest.getTelephone());
        medecin.setDiplomeUrl(signUpRequest.getDiplomeUrl());
        medecin.setLatitude(signUpRequest.getLatitude());
        medecin.setLongitude(signUpRequest.getLongitude());
        medecin.setUser(savedUser);
        medecinRepository.save(medecin);

        logger.info("Registered new MEDECIN user (Pending): {}", savedUser.getEmail());

        return new MessageResponse("Un email de validation a été envoyé, veuillez confirmer votre email.");
    }

    @Override
    @Transactional
    public MessageResponse registerLaboratoire(SignupLaboRequest signUpRequest) {
        checkEmailAndPasswords(signUpRequest.getEmail(), signUpRequest.getPassword(), signUpRequest.getConfirmPassword());

        User user = createUser(signUpRequest.getEmail(), signUpRequest.getPassword(), "LABORATOIRE", false, "PENDING");
        User savedUser = saveUserAndSendOtp(user);

        Laboratoire labo = new Laboratoire();
        labo.setNomLabo(signUpRequest.getNomLabo());
        labo.setAdresseComplete(signUpRequest.getAdresseComplete());
        labo.setVille(signUpRequest.getVille());
        labo.setTelephone(signUpRequest.getTelephone());
        labo.setAnalyses(signUpRequest.getAnalyses());
        labo.setRegistreCommerceUrl(signUpRequest.getRegistreCommerceUrl());
        labo.setAutorisationUrl(signUpRequest.getAutorisationUrl());
        labo.setCertificationUrl(signUpRequest.getCertificationUrl());
        labo.setLatitude(signUpRequest.getLatitude());
        labo.setLongitude(signUpRequest.getLongitude());
        labo.setUser(savedUser);
        laboratoireRepository.save(labo);

        logger.info("Registered new LABORATOIRE user (Pending): {}", savedUser.getEmail());

        return new MessageResponse("Un email de validation a été envoyé, veuillez confirmer votre email.");
    }

    @Override
    @Transactional
    public MessageResponse sendOtp(String email, String purpose) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Aucun compte trouvé avec cet email."));

        if ("EMAIL_VERIFICATION".equals(purpose) && Boolean.TRUE.equals(user.getIsEmailVerified())) {
            throw new RuntimeException("Cet email est déjà vérifié.");
        }

        try {
            String otp = otpService.generateAndStore(email, purpose);
            if ("PASSWORD_RESET".equals(purpose)) {
                emailService.sendPasswordResetEmail(email, otp);
            } else {
                emailService.sendVerificationEmail(email, otp);
            }
            logger.info("Sent OTP for {} to {}", purpose, email);
            return new MessageResponse("Un email avec le code a été envoyé.");
        } catch (Exception e) {
            logger.error("Failed to send OTP to {}", email, e);
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : "Erreur lors de l'envoi de l'email.");
        }
    }

    @Override
    @Transactional
    public java.util.Map<String, Object> verifyEmailOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Aucun compte trouvé avec cet email."));

        OtpValidationResult result = otpService.validateAndInvalidate(email, "EMAIL_VERIFICATION", otp);
        if (result == OtpValidationResult.EXPIRED) {
            throw new RuntimeException("Ce code est expiré. Veuillez demander un nouveau code.");
        } else if (result == OtpValidationResult.MAX_ATTEMPTS_REACHED) {
            throw new RuntimeException("Trop de tentatives. Veuillez demander un nouveau code.");
        } else if (result == OtpValidationResult.INVALID) {
            throw new RuntimeException("Code incorrect.");
        }

        user.setIsEmailVerified(true);
        userRepository.save(user);

        String role = user.getRoles().isEmpty() ? "" : user.getRoles().get(0).getName();
        String status = user.getStatus();

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("message", "Votre email a été vérifié avec succès.");
        response.put("role", role);
        response.put("status", status);
        return response;
    }

    @Override
    @Transactional
    public MessageResponse resendVerificationEmail(String email) {
        return sendOtp(email, "EMAIL_VERIFICATION");
    }

    @Override
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user != null) {
            try {
                String otp = otpService.generateAndStore(user.getEmail(), "PASSWORD_RESET");
                emailService.sendPasswordResetEmail(user.getEmail(), otp);
                logger.info("Password reset OTP email sent to {}", user.getEmail());
            } catch (Exception e) {
                logger.error("Failed to send password reset OTP to {}", user.getEmail(), e);
                throw new RuntimeException(e.getMessage());
            }
        } else {
            logger.warn("Password reset OTP requested for non-existing email: {}", request.getEmail());
        }
        return new MessageResponse("Si cette adresse email est associée à un compte, un code avec des instructions vous sera envoyé.");
    }

    @Override
    @Transactional
    public MessageResponse resetPassword(ResetPasswordOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Aucun compte trouvé."));

        OtpValidationResult result = otpService.validateAndInvalidate(request.getEmail(), "PASSWORD_RESET", request.getOtp());
        if (result == OtpValidationResult.EXPIRED) {
            throw new RuntimeException("Ce code est expiré. Veuillez demander une nouvelle réinitialisation.");
        } else if (result == OtpValidationResult.MAX_ATTEMPTS_REACHED) {
            throw new RuntimeException("Trop de tentatives. Veuillez demander une nouvelle réinitialisation.");
        } else if (result == OtpValidationResult.INVALID) {
            throw new RuntimeException("Code incorrect.");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Les mots de passe ne correspondent pas.");
        }

        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return new MessageResponse("Votre mot de passe a été modifié avec succès. Vous pouvez maintenant vous connecter.");
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
            throw new RuntimeException("Token Google invalide ou expiré. Veuillez réessayer.");
        }

        if (googlePayload == null || googlePayload.containsKey("error_description")) {
            throw new RuntimeException("Token Google invalide. Veuillez réessayer.");
        }

        String email = (String) googlePayload.get("email");

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Impossible de récupérer l'email depuis le compte Google.");
        }

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = createUser(email, UUID.randomUUID().toString(), "PATIENT", true, "APPROVED");
            newUser.setIsEmailVerified(true);
            newUser.setVerificationToken(null);
            User savedUser = userRepository.save(newUser);

            Patient patient = new Patient();
            patient.setPrenom((String) googlePayload.getOrDefault("given_name", ""));
            patient.setNom((String) googlePayload.getOrDefault("family_name", ""));
            patient.setUser(savedUser);
            patientRepository.save(patient);
            
            logger.info("Created new patient from Google login: {}", email);

            return savedUser;
        });

        // 3. Load user details and generate Sehati JWT
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);
        return new JwtResponse(jwt, userDetails.getId(), userDetails.getEmail(), roles);
    }
}
