package com.snackoverflow.recommendation.repository;

import com.snackoverflow.recommendation.entity.SnackRecommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SnackRecommendationRepository extends JpaRepository<SnackRecommendation, UUID> {
    Page<SnackRecommendation> findByCreatedById(UUID userId, Pageable pageable);
}
