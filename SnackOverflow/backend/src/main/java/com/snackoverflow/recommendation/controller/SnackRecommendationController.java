package com.snackoverflow.recommendation.controller;

import com.snackoverflow.common.ApiResponse;
import com.snackoverflow.recommendation.dto.CreateSnackRecommendationRequest;
import com.snackoverflow.recommendation.dto.SnackRecommendationResponse;
import com.snackoverflow.recommendation.dto.UpdateSnackRecommendationRequest;
import com.snackoverflow.recommendation.service.SnackRecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/snack-recommendations")
@RequiredArgsConstructor
public class SnackRecommendationController {

    private final SnackRecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SnackRecommendationResponse>>> getAll(
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(recommendationService.getAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SnackRecommendationResponse>> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(recommendationService.getOne(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SnackRecommendationResponse>> create(
            Authentication auth,
            @RequestBody @Valid CreateSnackRecommendationRequest request) {
        UUID userId = UUID.fromString(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(recommendationService.create(userId, request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SnackRecommendationResponse>> update(
            Authentication auth,
            @PathVariable UUID id,
            @RequestBody @Valid UpdateSnackRecommendationRequest request) {
        UUID userId = UUID.fromString(auth.getName());
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(ApiResponse.ok(
                recommendationService.update(id, userId, isAdmin, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            Authentication auth,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(auth.getName());
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        recommendationService.delete(id, userId, isAdmin);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
