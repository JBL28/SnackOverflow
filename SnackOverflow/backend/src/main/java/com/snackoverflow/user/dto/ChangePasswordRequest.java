package com.snackoverflow.user.dto;

import com.snackoverflow.common.validation.SafePassword;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @SafePassword String newPassword
) {}
