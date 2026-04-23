package com.sehati.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibilityResponseDTO {
    private boolean eligible;
    private boolean hasReviewed;
    private ReviewResponseDTO existingReview;
}
