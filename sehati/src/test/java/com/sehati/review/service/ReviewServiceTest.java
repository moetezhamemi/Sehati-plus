package com.sehati.review.service;

import com.sehati.common.exception.BusinessException;
import com.sehati.laboratoire.service.LaboratoireService;
import com.sehati.medecin.service.MedecinService;
import com.sehati.patient.entities.Patient;
import com.sehati.patient.repository.PatientRepository;
import com.sehati.review.dto.RatingSummaryDTO;
import com.sehati.review.dto.ReviewRequestDTO;
import com.sehati.review.dto.ReviewResponseDTO;
import com.sehati.review.entities.Review;
import com.sehati.review.entities.TargetType;
import com.sehati.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private MedecinService medecinService;
    @Mock
    private LaboratoireService laboratoireService;

    @InjectMocks
    private ReviewService reviewService;

    private Patient patient;
    private ReviewRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1L);
        patient.setPrenom("John");
        patient.setNom("Doe");

        requestDTO = new ReviewRequestDTO(1L, TargetType.MEDECIN, 5, "Great doctor!");
    }

    @Test
    void createReview_Success() {
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));
        when(medecinService.exists(1L)).thenReturn(true);
        when(reviewRepository.existsByReviewerIdAndTargetIdAndTargetType(1L, 1L, TargetType.MEDECIN)).thenReturn(false);
        
        Review savedReview = Review.builder()
                .id(100L)
                .rating(5)
                .comment("Great doctor!")
                .reviewer(patient)
                .targetId(1L)
                .targetType(TargetType.MEDECIN)
                .build();
        
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        ReviewResponseDTO response = reviewService.createReview(requestDTO, 1L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(5, response.getRating());
        assertEquals("John Doe", response.getReviewerName());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createReview_TargetNotFound() {
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));
        when(medecinService.exists(1L)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> 
            reviewService.createReview(requestDTO, 1L)
        );

        assertTrue(exception.getMessage().contains("does not exist"));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_DuplicateReview() {
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));
        when(medecinService.exists(1L)).thenReturn(true);
        when(reviewRepository.existsByReviewerIdAndTargetIdAndTargetType(1L, 1L, TargetType.MEDECIN)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> 
            reviewService.createReview(requestDTO, 1L)
        );

        assertTrue(exception.getMessage().contains("already reviewed"));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void getRatingSummary_Success() {
        RatingSummaryDTO expectedSummary = new RatingSummaryDTO(4.5, 10L);
        when(reviewRepository.getRatingSummary(1L, TargetType.MEDECIN)).thenReturn(expectedSummary);

        RatingSummaryDTO result = reviewService.getRatingSummary(1L, TargetType.MEDECIN);

        assertEquals(4.5, result.getAverageRating());
        assertEquals(10L, result.getReviewCount());
    }

    @Test
    void getRatingSummary_Empty() {
        when(reviewRepository.getRatingSummary(1L, TargetType.MEDECIN)).thenReturn(null);

        RatingSummaryDTO result = reviewService.getRatingSummary(1L, TargetType.MEDECIN);

        assertEquals(0.0, result.getAverageRating());
        assertEquals(0L, result.getReviewCount());
    }
}
