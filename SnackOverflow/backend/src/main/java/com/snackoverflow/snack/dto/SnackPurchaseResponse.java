package com.snackoverflow.snack.dto;

import com.snackoverflow.snack.entity.SnackPurchase;
import com.snackoverflow.snack.entity.SnackPurchaseStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SnackPurchaseResponse(
        UUID id,
        String name,
        SnackPurchaseStatus status,
        int likes,
        int dislikes,
        String createdByNickname,
        LocalDateTime createdAt
) {
    public static SnackPurchaseResponse from(SnackPurchase snack) {
        return new SnackPurchaseResponse(
                snack.getId(),
                snack.getName(),
                snack.getStatus(),
                snack.getLikes(),
                snack.getDislikes(),
                snack.getCreatedBy().getNickname(),
                snack.getCreatedAt()
        );
    }
}
