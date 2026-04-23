package com.sehati.review.repository;

import com.sehati.review.dto.RatingSummaryDTO;
import com.sehati.review.entities.Review;
import com.sehati.review.entities.TargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByTargetIdAndTargetType(Long targetId, TargetType targetType, Pageable pageable);

    Optional<Review> findByReviewerIdAndTargetIdAndTargetType(Long reviewerId, Long targetId, TargetType targetType);

    boolean existsByReviewerIdAndTargetIdAndTargetType(Long reviewerId, Long targetId, TargetType targetType);

    @Query("SELECT new com.sehati.review.dto.RatingSummaryDTO(CAST(AVG(r.rating) AS Double), COUNT(r.id)) " +
           "FROM Review r WHERE r.targetId = :targetId AND r.targetType = :targetType")
    RatingSummaryDTO getRatingSummary(Long targetId, TargetType targetType);
}
