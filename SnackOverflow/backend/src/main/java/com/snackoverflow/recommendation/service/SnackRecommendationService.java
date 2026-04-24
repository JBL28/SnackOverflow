package com.snackoverflow.recommendation.service;

import com.snackoverflow.recommendation.dto.CreateSnackRecommendationRequest;
import com.snackoverflow.recommendation.dto.SnackRecommendationResponse;
import com.snackoverflow.recommendation.dto.UpdateSnackRecommendationRequest;
import com.snackoverflow.recommendation.entity.SnackRecommendation;
import com.snackoverflow.recommendation.repository.SnackRecommendationRepository;
import com.snackoverflow.user.entity.User;
import com.snackoverflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SnackRecommendationService {

    private final SnackRecommendationRepository recommendationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<SnackRecommendationResponse> getAll(Pageable pageable) {
        return recommendationRepository.findAll(pageable).map(SnackRecommendationResponse::from);
    }

    @Transactional(readOnly = true)
    public SnackRecommendationResponse getOne(UUID id) {
        return SnackRecommendationResponse.from(findById(id));
    }

    @Transactional
    public SnackRecommendationResponse create(UUID userId, CreateSnackRecommendationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        SnackRecommendation rec = SnackRecommendation.builder()
                .name(request.name())
                .reason(request.reason())
                .createdBy(user)
                .build();
        return SnackRecommendationResponse.from(recommendationRepository.save(rec));
    }

    @Transactional
    public SnackRecommendationResponse update(UUID id, UUID userId, boolean isAdmin,
                                              UpdateSnackRecommendationRequest request) {
        SnackRecommendation rec = findById(id);
        if (!isAdmin && !rec.getCreatedBy().getId().equals(userId)) {
            throw new SecurityException("수정 권한이 없습니다.");
        }
        rec.update(request.name(), request.reason());
        return SnackRecommendationResponse.from(rec);
    }

    @Transactional
    public void delete(UUID id, UUID userId, boolean isAdmin) {
        SnackRecommendation rec = findById(id);
        if (!isAdmin && !rec.getCreatedBy().getId().equals(userId)) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }
        recommendationRepository.delete(rec);
    }

    private SnackRecommendation findById(UUID id) {
        return recommendationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("추천 게시글을 찾을 수 없습니다."));
    }
}
