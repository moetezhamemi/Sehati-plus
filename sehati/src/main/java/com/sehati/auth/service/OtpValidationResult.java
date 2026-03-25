package com.sehati.auth.service;

public enum OtpValidationResult {
    VALID,
    INVALID,
    EXPIRED,
    MAX_ATTEMPTS_REACHED
}
