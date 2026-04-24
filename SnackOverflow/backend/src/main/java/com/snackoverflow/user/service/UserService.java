package com.snackoverflow.user.service;

import com.snackoverflow.auth.repository.RefreshTokenRepository;
import com.snackoverflow.comment.dto.CommentResponse;
import com.snackoverflow.comment.repository.CommentRepository;
import com.snackoverflow.recommendation.dto.SnackRecommendationResponse;
import com.snackoverflow.recommendation.repository.SnackRecommendationRepository;
import com.snackoverflow.user.dto.AdminChangeStatusRequest;
import com.snackoverflow.user.dto.AdminResetPasswordRequest;
import com.snackoverflow.user.dto.AdminUserResponse;
import com.snackoverflow.user.dto.ChangePasswordRequest;
import com.snackoverflow.user.dto.UpdateNicknameRequest;
import com.snackoverflow.user.dto.UserSummaryResponse;
import com.snackoverflow.user.entity.User;
import com.snackoverflow.user.entity.UserStatus;
import com.snackoverflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SnackRecommendationRepository snackRecommendationRepository;
    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public UserSummaryResponse getMyProfile(UUID userId) {
        return UserSummaryResponse.from(findActiveUser(userId));
    }

    @Transactional
    public UserSummaryResponse updateNickname(UUID userId, UpdateNicknameRequest request) {
        User user = findActiveUser(userId);
        user.updateNickname(request.nickname());
        return UserSummaryResponse.from(user);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = findActiveUser(userId);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        user.updatePasswordHash(passwordEncoder.encode(request.newPassword()));
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    @Transactional
    public void withdraw(UUID userId) {
        User user = findActiveUser(userId);
        user.updateStatus(UserStatus.DELETED);
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<SnackRecommendationResponse> getMyPosts(UUID userId) {
        return snackRecommendationRepository
                .findByCreatedByIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(SnackRecommendationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getMyComments(UUID userId) {
        return commentRepository
                .findByAuthorIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(CommentResponse::from)
                .toList();
    }

    // ── 관리자 전용 ──────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(AdminUserResponse::from);
    }

    @Transactional
    public AdminUserResponse changeUserStatus(UUID userId, AdminChangeStatusRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.updateStatus(request.status());
        return AdminUserResponse.from(user);
    }

    @Transactional
    public void resetUserPassword(UUID userId, AdminResetPasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.updatePasswordHash(passwordEncoder.encode(request.newPassword()));
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    private User findActiveUser(UUID userId) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}