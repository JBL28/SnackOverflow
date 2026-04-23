package com.snackoverflow.snack.dto;

import com.snackoverflow.snack.entity.SnackPurchaseStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull(message = "상태 값을 입력해주세요.")
        SnackPurchaseStatus status
) {}
