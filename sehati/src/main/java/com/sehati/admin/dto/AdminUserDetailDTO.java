package com.sehati.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailDTO {
    private Long userId;
    private String role;
    private String status;
    private String email;
    private String photoProfilUrl;
    private Boolean enabled;
}
