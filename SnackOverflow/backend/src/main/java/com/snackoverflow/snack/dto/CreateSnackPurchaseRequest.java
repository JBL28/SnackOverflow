package com.snackoverflow.snack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSnackPurchaseRequest(
        @NotBlank(message = "과자 이름을 입력해주세요.")
        @Size(max = 100, message = "과자 이름은 100자 이하여야 합니다.")
        String name
) {}
