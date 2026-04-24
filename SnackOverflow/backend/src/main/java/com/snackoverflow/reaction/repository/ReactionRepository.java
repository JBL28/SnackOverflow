package com.snackoverflow.reaction.repository;

import com.snackoverflow.reaction.entity.Reaction;
import com.snackoverflow.reaction.entity.ReactionTargetType;
import com.snackoverflow.reaction.entity.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReactionRepository extends JpaRepository<Reaction, UUID> {

    Optional<Reaction> findByUserIdAndTargetTypeAndTargetId(
            UUID userId, ReactionTargetType targetType, UUID targetId);

    @Query("SELECT r.user.nickname FROM Reaction r " +
           "WHERE r.targetType = :targetType AND r.targetId = :targetId AND r.type = :type")
    List<String> findNicknamesByTargetTypeAndTargetIdAndType(
            ReactionTargetType targetType, UUID targetId, ReactionType type);
}