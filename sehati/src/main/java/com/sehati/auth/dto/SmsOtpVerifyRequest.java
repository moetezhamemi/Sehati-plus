package com.sehati.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SmsOtpVerifyRequest {
    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    private String telephone;

    @NotBlank(message = "Le code est obligatoire")
    private String code;

    @NotBlank(message = "L'email est obligatoire")
    private String email;
}
