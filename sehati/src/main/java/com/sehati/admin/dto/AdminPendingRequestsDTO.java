package com.sehati.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPendingRequestsDTO {
    private long totalPending;
    private long pendingMedecins;
    private long pendingLaboratoires;
}
