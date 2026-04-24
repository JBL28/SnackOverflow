package com.snackoverflow.user.dto;

import com.snackoverflow.user.entity.User;
import com.snackoverflow.user.entity.UserRole;
import com.snackoverflow.user.entity.UserStatus;

import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String username,
        String nickname,
        int postCount,
        int commentCount,
        UserStatus status,
        UserRole role
) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getPostCount(),
                user.getCommentCount(),
                user.getStatus(),
                user.getRole()
        );
    }
}
