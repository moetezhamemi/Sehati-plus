package com.sehati.common.service;

import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioSmsService {

    private static final Logger logger = LoggerFactory.getLogger(TwilioSmsService.class);
    private static final String COUNTRY_PREFIX = "+216";

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.service-sid}")
    private String serviceSid;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
        logger.info("Twilio SDK initialized successfully");
    }

    /**
     * Formate un numéro tunisien de 8 chiffres en format E.164.
     */
    public String formatPhone(String phone) {
        if (phone == null) return null;
        String cleaned = phone.replaceAll("[^0-9]", "");
        if (cleaned.startsWith("216")) {
            return "+" + cleaned;
        }
        return COUNTRY_PREFIX + cleaned;
    }

    /**
     * Envoie un code de vérification SMS via Twilio Verify.
     */
    public void sendVerificationCode(String phone) {
        String formattedPhone = formatPhone(phone);
        logger.info("Sending SMS verification to {} (in French)", formattedPhone);
        Verification.creator(serviceSid, formattedPhone, "sms")
            .setLocale("fr")
            .create();
        logger.info("SMS verification sent successfully to {}", formattedPhone);
    }

    /**
     * Vérifie le code OTP saisi par l'utilisateur via Twilio Verify.
     * @return true si le code est valide ("approved"), false sinon
     */
    public boolean verifyCode(String phone, String code) {
        String formattedPhone = formatPhone(phone);
        logger.info("Verifying SMS code for {}", formattedPhone);
        VerificationCheck check = VerificationCheck.creator(serviceSid)
                .setTo(formattedPhone)
                .setCode(code)
                .create();
        boolean approved = "approved".equals(check.getStatus());
        logger.info("SMS verification for {} result: {}", formattedPhone, check.getStatus());
        return approved;
    }
}
