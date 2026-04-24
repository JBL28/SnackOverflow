package com.snackoverflow.reaction.controller;

import com.snackoverflow.common.ApiResponse;
import com.snackoverflow.reaction.dto.ToggleReactionRequest;
import com.snackoverflow.reaction.dto.ToggleReactionResponse;
import com.snackoverflow.reaction.dto.VotersResponse;
import com.snackoverflow.reaction.entity.ReactionTargetType;
import com.snackoverflow.reaction.entity.ReactionType;
import com.snackoverflow.reaction.service.ReactionService;
import com.snackoverflow.security.CustomUserDetailsService;
import com.snackoverflow.user.entity.User;
import com.snackoverflow.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<ToggleReactionResponse>> toggle(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ToggleReactionRequest request) {
        User user = userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        var result = reactionService.toggle(user, request.targetType(), request.targetId(), request.type());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/voters")
    public ResponseEntity<ApiResponse<VotersResponse>> getVoters(
            @RequestParam ReactionTargetType targetType,
            @RequestParam UUID targetId,
            @RequestParam ReactionType type) {
        return ResponseEntity.ok(ApiResponse.ok(reactionService.getVoters(targetType, targetId, type)));
    }
}