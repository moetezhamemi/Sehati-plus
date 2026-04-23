package com.sehati.review.service;

import com.sehati.common.exception.BusinessException;
import com.sehati.laboratoire.service.LaboratoireService;
import com.sehati.medecin.service.MedecinService;
import com.sehati.patient.entities.Patient;
import com.sehati.patient.repository.PatientRepository;
import com.sehati.review.dto.RatingSummaryDTO;
import com.sehati.review.dto.ReviewRequestDTO;
import com.sehati.review.dto.ReviewResponseDTO;
import com.sehati.review.dto.EligibilityResponseDTO;
import com.sehati.review.entities.Review;
import com.sehati.review.entities.TargetType;
import com.sehati.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PatientRepository patientRepository;
    private final MedecinService medecinService;
    private final LaboratoireService laboratoireService;

    @Transactional
    public ReviewResponseDTO createReview(ReviewRequestDTO request, Long userId) {
        // 1. Resolve Patient from current User
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Only registered patients can create reviews."));

        // 2. Validate Target Existence via Domain Services
        validateTargetExistence(request.getTargetId(), request.getTargetType());

        // 3. Prevent Duplicate Reviews (One review per patient per target)
        if (reviewRepository.existsByReviewerIdAndTargetIdAndTargetType(
                patient.getId(), request.getTargetId(), request.getTargetType())) {
            throw new BusinessException("You have already reviewed this " + request.getTargetType().name().toLowerCase() + ".");
        }

        // 4. Create and Save Review
        Review review = Review.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .reviewer(patient)
                .targetId(request.getTargetId())
                .targetType(request.getTargetType())
                .build();

        Review savedReview = reviewRepository.save(review);
        return mapToResponseDTO(savedReview);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> getReviewsByTarget(Long targetId, TargetType targetType, Pageable pageable) {
        return reviewRepository.findByTargetIdAndTargetType(targetId, targetType, pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public RatingSummaryDTO getRatingSummary(Long targetId, TargetType targetType) {
        RatingSummaryDTO summary = reviewRepository.getRatingSummary(targetId, targetType);
        if (summary == null || summary.getReviewCount() == 0) {
            return new RatingSummaryDTO(0.0, 0L);
        }
        return summary;
    }

    @Transactional(readOnly = true)
    public EligibilityResponseDTO checkEligibility(Long targetId, TargetType targetType, Long userId) {
        // 1. Resolve Patient
        var patientOpt = patientRepository.findByUserId(userId);
        if (patientOpt.isEmpty()) {
            return new EligibilityResponseDTO(false, false, null); // Only patients can review
        }
        Patient patient = patientOpt.get();

        // 2. Validate Target Existence
        try {
            validateTargetExistence(targetId, targetType);
        } catch (BusinessException e) {
            return new EligibilityResponseDTO(false, false, null);
        }

        // 3. Must have had an appointment with the target
        // TODO: replace with real appointment check when Appointment module is ready
        boolean hasCompletedAppointment = true; // Mocked properly

        if (!hasCompletedAppointment) {
            return new EligibilityResponseDTO(false, false, null);
        }

        // 4. Check for existing review
        var existingReview = reviewRepository.findByReviewerIdAndTargetIdAndTargetType(
                patient.getId(), targetId, targetType);

        if (existingReview.isPresent()) {
            return new EligibilityResponseDTO(true, true, mapToResponseDTO(existingReview.get()));
        }

        return new EligibilityResponseDTO(true, false, null);
    }

    @Transactional
    public ReviewResponseDTO updateReview(Long reviewId, ReviewRequestDTO request, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException("Review not found."));

        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("User is not a patient."));

        if (!review.getReviewer().getId().equals(patient.getId())) {
            throw new BusinessException("You can only modify your own reviews.");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        
        return mapToResponseDTO(reviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException("Review not found."));

        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("User is not a patient."));

        if (!review.getReviewer().getId().equals(patient.getId())) {
            throw new BusinessException("You can only delete your own reviews.");
        }

        reviewRepository.delete(review);
    }

    private void validateTargetExistence(Long targetId, TargetType targetType) {
        boolean exists = switch (targetType) {
            case MEDECIN -> medecinService.exists(targetId);
            case LABORATORY -> laboratoireService.exists(targetId);
        };

        if (!exists) {
            throw new BusinessException("The targeted " + targetType.name().toLowerCase() + " does not exist.");
        }
    }

    private ReviewResponseDTO mapToResponseDTO(Review review) {
        return new ReviewResponseDTO(
                review.getId(),
                review.getTargetId(),
                review.getTargetType(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getReviewer().getPrenom() + " " + review.getReviewer().getNom()
        );
    }
}
