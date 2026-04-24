package com.snackoverflow.comment.controller;

import com.snackoverflow.comment.dto.CommentResponse;
import com.snackoverflow.comment.dto.CreateCommentRequest;
import com.snackoverflow.comment.dto.UpdateCommentRequest;
import com.snackoverflow.comment.entity.TargetType;
import com.snackoverflow.comment.service.CommentService;
import com.snackoverflow.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @RequestParam TargetType targetType,
            @RequestParam UUID targetId) {
        return ResponseEntity.ok(ApiResponse.ok(commentService.getComments(targetType, targetId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> create(
            Authentication auth,
            @RequestBody @Valid CreateCommentRequest request) {
        UUID userId = UUID.fromString(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(commentService.create(userId, request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CommentResponse>> update(
            Authentication auth,
            @PathVariable UUID id,
            @RequestBody @Valid UpdateCommentRequest request) {
        UUID userId = UUID.fromString(auth.getName());
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(ApiResponse.ok(
                commentService.update(id, userId, isAdmin, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            Authentication auth,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(auth.getName());
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        commentService.delete(id, userId, isAdmin);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}