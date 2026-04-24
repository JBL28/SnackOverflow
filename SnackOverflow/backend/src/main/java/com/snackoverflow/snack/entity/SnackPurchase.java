package com.snackoverflow.snack.entity;

import com.snackoverflow.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "snack_purchases")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SnackPurchase {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private SnackPurchaseStatus status = SnackPurchaseStatus.IN_STOCK;

    @Column(nullable = false)
    @Builder.Default
    private int likes = 0;

    @Column(nullable = false)
    @Builder.Default
    private int dislikes = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false,
            columnDefinition = "BINARY(16)")
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    private void onCreate() {
        if (id == null) id = UUID.randomUUID();
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateStatus(SnackPurchaseStatus status) {
        this.status = status;
    }

    public void adjustLikes(int delta) {
        this.likes = Math.max(0, this.likes + delta);
    }

    public void adjustDislikes(int delta) {
        this.dislikes = Math.max(0, this.dislikes + delta);
    }
}
