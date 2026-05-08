package com.sehati.common.exception;

import lombok.Getter;

/**
 * Exception thrown when a phone number update requires SMS verification
 * because an orphan patient (without a User) already exists with that number.
 */
@Getter
public class PhoneVerificationRequiredException extends RuntimeException {

    private final Long orphanPatientId;

    public PhoneVerificationRequiredException(String message, Long orphanPatientId) {
        super(message);
        this.orphanPatientId = orphanPatientId;
    }
}
