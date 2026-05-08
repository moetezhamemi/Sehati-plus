package com.sehati.medecin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardReviewDTO {
    private Long id;
    private String reviewerName;
    private String reviewerPhotoUrl;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
