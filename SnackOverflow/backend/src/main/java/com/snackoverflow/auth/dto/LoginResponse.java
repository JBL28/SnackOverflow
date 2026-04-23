package com.snackoverflow.auth.dto;

import com.snackoverflow.user.entity.UserRole;

import java.util.UUID;

public record LoginResponse(
        String accessToken,
        long accessExpiresIn,
        UUID userId,
        String username,
        String nickname,
        UserRole role
) {}
