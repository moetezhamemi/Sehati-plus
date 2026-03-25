package com.sehati.auth.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class JwtResponse {
    private final String token;
    private final String type = "Bearer";
    private final Long id;
    private final String email;
    private final List<String> roles;

    public JwtResponse(String accessToken, Long id, String email, List<String> roles) {
        this.token = accessToken;
        this.id = id;
        this.email = email;
        this.roles = roles;
    }
}
