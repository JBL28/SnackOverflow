package com.snackoverflow.recommendation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSnackRecommendationRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 1000) String reason
) {}
