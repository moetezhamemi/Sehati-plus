package com.sehati.review.controller;

import com.sehati.auth.security.UserDetailsImpl;
import com.sehati.review.dto.RatingSummaryDTO;
import com.sehati.review.dto.ReviewRequestDTO;
import com.sehati.review.dto.ReviewResponseDTO;
import com.sehati.review.dto.EligibilityResponseDTO;
import com.sehati.review.entities.TargetType;
import com.sehati.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<ReviewResponseDTO> createReview(
            @Valid @RequestBody ReviewRequestDTO request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return new ResponseEntity<>(reviewService.createReview(request, userDetails.getId()), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<ReviewResponseDTO>> getReviews(
            @RequestParam Long targetId,
            @RequestParam TargetType targetType,
            Pageable pageable) {
        return ResponseEntity.ok(reviewService.getReviewsByTarget(targetId, targetType, pageable));
    }

    @GetMapping("/summary")
    public ResponseEntity<RatingSummaryDTO> getSummary(
            @RequestParam Long targetId,
            @RequestParam TargetType targetType) {
        return ResponseEntity.ok(reviewService.getRatingSummary(targetId, targetType));
    }

    @GetMapping("/eligibility")
    public ResponseEntity<EligibilityResponseDTO> checkEligibility(
            @RequestParam Long targetId,
            @RequestParam TargetType targetType,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.ok(new EligibilityResponseDTO(false, false, null));
        }
        return ResponseEntity.ok(reviewService.checkEligibility(targetId, targetType, userDetails.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequestDTO request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(reviewService.updateReview(id, request, userDetails.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        reviewService.deleteReview(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
