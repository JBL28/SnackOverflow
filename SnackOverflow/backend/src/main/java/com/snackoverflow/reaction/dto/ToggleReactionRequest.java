package com.snackoverflow.reaction.dto;

import com.snackoverflow.reaction.entity.ReactionTargetType;
import com.snackoverflow.reaction.entity.ReactionType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ToggleReactionRequest(
        @NotNull ReactionTargetType targetType,
        @NotNull UUID targetId,
        @NotNull ReactionType type
) {}