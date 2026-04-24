package com.snackoverflow.comment.entity;

import com.snackoverflow.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Comment {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false, updatable = false,
            columnDefinition = "BINARY(16)")
    private User author;

    @Column(name = "parent_id", columnDefinition = "BINARY(16)")
    private UUID parentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID targetId;

    @Column(nullable = false)
    @Builder.Default
    private int likes = 0;

    @Column(nullable = false)
    @Builder.Default
    private int dislikes = 0;

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

    public void updateContent(String content) {
        this.content = content;
    }

    public void adjustLikes(int delta) {
        this.likes = Math.max(0, this.likes + delta);
    }

    public void adjustDislikes(int delta) {
        this.dislikes = Math.max(0, this.dislikes + delta);
    }
}