package com.snackoverflow.recommendation.dto;

import com.snackoverflow.recommendation.entity.SnackRecommendation;

import java.time.LocalDateTime;
import java.util.UUID;

public record SnackRecommendationResponse(
        UUID id,
        String name,
        String reason,
        int likes,
        int dislikes,
        String createdByNickname,
        UUID createdById,
        LocalDateTime createdAt
) {
    public static SnackRecommendationResponse from(SnackRecommendation r) {
        return new SnackRecommendationResponse(
                r.getId(),
                r.getName(),
                r.getReason(),
                r.getLikes(),
                r.getDislikes(),
                r.getCreatedBy().getNickname(),
                r.getCreatedBy().getId(),
                r.getCreatedAt()
        );
    }
}
