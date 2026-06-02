package com.sehati.support.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactMessageRequestDTO {
    private String fullName;
    private String email;
    private String phone;
    private String subject;
    private String message;
}
