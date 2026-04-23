package com.sehati.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingSummaryDTO {
    private Double averageRating;
    private Long reviewCount;
}
