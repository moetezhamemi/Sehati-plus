package com.sehati.auth.service;

public interface OtpService {
    /**
     * Generates a 6-digit OTP, BCrypt-hashes it, stores it in Redis, and enforces hourly rate limits.
     * @param email The user's email
     * @param purpose The purpose of the OTP (e.g., EMAIL_VERIFICATION, PASSWORD_RESET)
     * @return The plaintext 6-digit code to be sent via email
     */
    String generateAndStore(String email, String purpose);

    /**
     * Pure validation logic: checks hash, attempts, and expiry. Does NOT invalidate the OTP on success.
     */
    OtpValidationResult validateOnly(String email, String purpose, String code);

    /**
     * Validates the OTP and, if valid, deletes it from Redis so it is single-use.
     */
    OtpValidationResult validateAndInvalidate(String email, String purpose, String code);

    /**
     * Checks if the 60s resend cooldown is active.
     */
    boolean isCooldownActive(String email, String purpose);

    /**
     * Starts the 60s resend cooldown.
     */
    void startCooldown(String email, String purpose);
}
