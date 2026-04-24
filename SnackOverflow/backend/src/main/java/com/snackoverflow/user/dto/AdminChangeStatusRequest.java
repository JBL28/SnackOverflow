package com.snackoverflow.user.dto;

import com.snackoverflow.user.entity.UserStatus;
import jakarta.validation.constraints.NotNull;

public record AdminChangeStatusRequest(
        @NotNull UserStatus status
) {}
