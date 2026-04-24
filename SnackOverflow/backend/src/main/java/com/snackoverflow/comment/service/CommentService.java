package com.snackoverflow.comment.service;

import com.snackoverflow.comment.dto.CommentResponse;
import com.snackoverflow.comment.dto.CreateCommentRequest;
import com.snackoverflow.comment.dto.UpdateCommentRequest;
import com.snackoverflow.comment.entity.Comment;
import com.snackoverflow.comment.entity.TargetType;
import com.snackoverflow.comment.repository.CommentRepository;
import com.snackoverflow.user.entity.User;
import com.snackoverflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(TargetType targetType, UUID targetId) {
        return commentRepository
                .findByTargetTypeAndTargetIdOrderByCreatedAtAsc(targetType, targetId)
                .stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public CommentResponse create(UUID userId, CreateCommentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Comment comment = Comment.builder()
                .content(request.content())
                .author(user)
                .parentId(request.parentId())
                .targetType(request.targetType())
                .targetId(request.targetId())
                .build();
        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional
    public CommentResponse update(UUID commentId, UUID userId, boolean isAdmin,
                                  UpdateCommentRequest request) {
        Comment comment = findById(commentId);
        if (!isAdmin && !comment.getAuthor().getId().equals(userId)) {
            throw new SecurityException("수정 권한이 없습니다.");
        }
        comment.updateContent(request.content());
        return CommentResponse.from(comment);
    }

    @Transactional
    public void delete(UUID commentId, UUID userId, boolean isAdmin) {
        Comment comment = findById(commentId);
        if (!isAdmin && !comment.getAuthor().getId().equals(userId)) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }
        commentRepository.delete(comment);
    }

    private Comment findById(UUID id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
    }
}