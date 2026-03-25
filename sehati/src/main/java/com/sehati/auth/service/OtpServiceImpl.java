package com.sehati.auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class OtpServiceImpl implements OtpService {

    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final int OTP_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 5;
    private static final int HOURLY_LIMIT = 7;
    
    // TTLS
    private static final Duration OTP_TTL = Duration.ofMinutes(10);
    private static final Duration COOLDOWN_TTL = Duration.ofSeconds(60);
    private static final Duration HOURLY_TTL = Duration.ofHours(1);

    public OtpServiceImpl(StringRedisTemplate redisTemplate, PasswordEncoder passwordEncoder) {
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }

    private String getKeyValue(String purpose, String email) {
        return "otp:" + purpose + ":" + email;
    }
    
    private String getKeyAttempts(String purpose, String email) {
        return "otp:" + purpose + ":" + email + ":attempts";
    }

    private String getKeyCooldown(String purpose, String email) {
        return "otp:" + purpose + ":" + email + ":cooldown";
    }

    private String getKeyHourly(String purpose, String email) {
        return "otp:" + purpose + ":" + email + ":hourly";
    }

    @Override
    public String generateAndStore(String email, String purpose) {
        String hourlyKey = getKeyHourly(purpose, email);
        String hourlyCountStr = redisTemplate.opsForValue().get(hourlyKey);
        int hourlyCount = hourlyCountStr != null ? Integer.parseInt(hourlyCountStr) : 0;
        
        if (hourlyCount >= HOURLY_LIMIT) {
            throw new RuntimeException("Limite de demandes atteinte. Veuillez réessayer dans une heure.");
        }

        String rawCode = generateRandomCode();
        String hashedCode = passwordEncoder.encode(rawCode);

        String valueKey = getKeyValue(purpose, email);
        String attemptsKey = getKeyAttempts(purpose, email);

        // Store hash and reset attempts
        redisTemplate.opsForValue().set(valueKey, hashedCode, OTP_TTL);
        redisTemplate.opsForValue().set(attemptsKey, "0", OTP_TTL);
        
        // Start cooldown
        startCooldown(email, purpose);

        // Increment hourly rate limit
        if (hourlyCountStr == null) {
            redisTemplate.opsForValue().set(hourlyKey, "1", HOURLY_TTL);
        } else {
            redisTemplate.opsForValue().increment(hourlyKey);
        }

        return rawCode;
    }

    @Override
    public OtpValidationResult validateOnly(String email, String purpose, String code) {
        String valueKey = getKeyValue(purpose, email);
        String attemptsKey = getKeyAttempts(purpose, email);

        String storedHash = redisTemplate.opsForValue().get(valueKey);
        if (storedHash == null) {
            return OtpValidationResult.EXPIRED;
        }

        String attemptsStr = redisTemplate.opsForValue().get(attemptsKey);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;

        if (attempts >= MAX_ATTEMPTS) {
            return OtpValidationResult.MAX_ATTEMPTS_REACHED;
        }

        if (passwordEncoder.matches(code, storedHash)) {
            return OtpValidationResult.VALID;
        } else {
            redisTemplate.opsForValue().increment(attemptsKey);
            return OtpValidationResult.INVALID;
        }
    }

    @Override
    public OtpValidationResult validateAndInvalidate(String email, String purpose, String code) {
        OtpValidationResult result = validateOnly(email, purpose, code);
        
        if (result == OtpValidationResult.VALID) {
            String valueKey = getKeyValue(purpose, email);
            String attemptsKey = getKeyAttempts(purpose, email);
            String cooldownKey = getKeyCooldown(purpose, email);
            
            redisTemplate.delete(valueKey);
            redisTemplate.delete(attemptsKey);
            redisTemplate.delete(cooldownKey);
        }
        
        return result;
    }

    @Override
    public boolean isCooldownActive(String email, String purpose) {
        String cooldownKey = getKeyCooldown(purpose, email);
        return Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey));
    }

    @Override
    public void startCooldown(String email, String purpose) {
        String cooldownKey = getKeyCooldown(purpose, email);
        redisTemplate.opsForValue().set(cooldownKey, "1", COOLDOWN_TTL);
    }
}
