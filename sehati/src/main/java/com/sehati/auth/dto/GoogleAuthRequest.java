package com.sehati.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GoogleAuthRequest {

    @NotBlank(message = "Le token Google est obligatoire.")
    private String idToken;
}
