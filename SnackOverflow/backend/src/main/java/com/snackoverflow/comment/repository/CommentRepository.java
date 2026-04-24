package com.snackoverflow.comment.repository;

import com.snackoverflow.comment.entity.Comment;
import com.snackoverflow.comment.entity.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByTargetTypeAndTargetIdOrderByCreatedAtAsc(TargetType targetType, UUID targetId);
    List<Comment> findByAuthorIdOrderByCreatedAtDesc(UUID authorId);
}