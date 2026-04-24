package com.snackoverflow.comment.dto;

import com.snackoverflow.comment.entity.Comment;
import com.snackoverflow.comment.entity.TargetType;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        String content,
        String authorNickname,
        UUID authorId,
        UUID parentId,
        TargetType targetType,
        UUID targetId,
        int likes,
        int dislikes,
        LocalDateTime createdAt
) {
    public static CommentResponse from(Comment c) {
        return new CommentResponse(
                c.getId(),
                c.getContent(),
                c.getAuthor().getNickname(),
                c.getAuthor().getId(),
                c.getParentId(),
                c.getTargetType(),
                c.getTargetId(),
                c.getLikes(),
                c.getDislikes(),
                c.getCreatedAt()
        );
    }
}