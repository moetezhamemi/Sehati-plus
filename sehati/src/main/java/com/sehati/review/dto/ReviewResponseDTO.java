package com.sehati.review.dto;

import com.sehati.review.entities.TargetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {
    private Long id;
    private Long targetId;
    private TargetType targetType;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private String reviewerName;
}
