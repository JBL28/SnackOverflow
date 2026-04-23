package com.snackoverflow.user.dto;

import com.snackoverflow.user.entity.User;
import com.snackoverflow.user.entity.UserRole;
import com.snackoverflow.user.entity.UserStatus;

import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String username,
        String nickname,
        String email,
        UserRole role,
        UserStatus status,
        int postCount,
        int commentCount,
        boolean mustChangePassword
) {
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getPostCount(),
                user.getCommentCount(),
                user.isMustChangePassword()
        );
    }
}
