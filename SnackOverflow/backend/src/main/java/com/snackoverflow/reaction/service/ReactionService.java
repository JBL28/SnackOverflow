package com.snackoverflow.reaction.service;

import com.snackoverflow.comment.entity.Comment;
import com.snackoverflow.comment.repository.CommentRepository;
import com.snackoverflow.reaction.dto.ToggleReactionResponse;
import com.snackoverflow.reaction.dto.VotersResponse;
import com.snackoverflow.reaction.entity.Reaction;
import com.snackoverflow.reaction.entity.ReactionTargetType;
import com.snackoverflow.reaction.entity.ReactionType;
import com.snackoverflow.reaction.repository.ReactionRepository;
import com.snackoverflow.recommendation.entity.SnackRecommendation;
import com.snackoverflow.recommendation.repository.SnackRecommendationRepository;
import com.snackoverflow.snack.entity.SnackPurchase;
import com.snackoverflow.snack.repository.SnackPurchaseRepository;
import com.snackoverflow.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final SnackPurchaseRepository snackPurchaseRepository;
    private final SnackRecommendationRepository snackRecommendationRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public ToggleReactionResponse toggle(User user, ReactionTargetType targetType, UUID targetId, ReactionType type) {
        var existing = reactionRepository.findByUserIdAndTargetTypeAndTargetId(user.getId(), targetType, targetId);

        if (existing.isEmpty()) {
            var reaction = Reaction.builder()
                    .user(user)
                    .targetType(targetType)
                    .targetId(targetId)
                    .type(type)
                    .build();
            reactionRepository.save(reaction);
            adjustCount(targetType, targetId, type, +1);
        } else {
            Reaction reaction = existing.get();
            if (reaction.getType() == type) {
                reactionRepository.delete(reaction);
                adjustCount(targetType, targetId, type, -1);
            } else {
                adjustCount(targetType, targetId, reaction.getType(), -1);
                reaction.changeType(type);
                adjustCount(targetType, targetId, type, +1);
            }
        }

        return buildResponse(targetType, targetId, user.getId());
    }

    @Transactional(readOnly = true)
    public VotersResponse getVoters(ReactionTargetType targetType, UUID targetId, ReactionType type) {
        List<String> nicknames = reactionRepository
                .findNicknamesByTargetTypeAndTargetIdAndType(targetType, targetId, type);
        return new VotersResponse(nicknames);
    }

    private void adjustCount(ReactionTargetType targetType, UUID targetId, ReactionType type, int delta) {
        switch (targetType) {
            case SNACK_PURCHASE -> {
                SnackPurchase snack = snackPurchaseRepository.findById(targetId)
                        .orElseThrow(() -> new IllegalArgumentException("Snack not found"));
                if (type == ReactionType.LIKE) snack.adjustLikes(delta);
                else snack.adjustDislikes(delta);
            }
            case RECOMMENDATION -> {
                SnackRecommendation rec = snackRecommendationRepository.findById(targetId)
                        .orElseThrow(() -> new IllegalArgumentException("Recommendation not found"));
                if (type == ReactionType.LIKE) rec.adjustLikes(delta);
                else rec.adjustDislikes(delta);
            }
            case COMMENT -> {
                Comment comment = commentRepository.findById(targetId)
                        .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
                if (type == ReactionType.LIKE) comment.adjustLikes(delta);
                else comment.adjustDislikes(delta);
            }
        }
    }

    private ToggleReactionResponse buildResponse(ReactionTargetType targetType, UUID targetId, UUID userId) {
        int likes, dislikes;
        switch (targetType) {
            case SNACK_PURCHASE -> {
                SnackPurchase snack = snackPurchaseRepository.findById(targetId).orElseThrow();
                likes = snack.getLikes();
                dislikes = snack.getDislikes();
            }
            case RECOMMENDATION -> {
                SnackRecommendation rec = snackRecommendationRepository.findById(targetId).orElseThrow();
                likes = rec.getLikes();
                dislikes = rec.getDislikes();
            }
            case COMMENT -> {
                Comment comment = commentRepository.findById(targetId).orElseThrow();
                likes = comment.getLikes();
                dislikes = comment.getDislikes();
            }
            default -> throw new IllegalArgumentException("Unknown target type");
        }
        String myReaction = reactionRepository
                .findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId)
                .map(r -> r.getType().name())
                .orElse(null);
        return new ToggleReactionResponse(likes, dislikes, myReaction);
    }
}