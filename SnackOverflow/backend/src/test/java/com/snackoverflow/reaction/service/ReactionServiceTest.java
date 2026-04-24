package com.snackoverflow.reaction.service;

import com.snackoverflow.comment.repository.CommentRepository;
import com.snackoverflow.reaction.entity.Reaction;
import com.snackoverflow.reaction.entity.ReactionTargetType;
import com.snackoverflow.reaction.entity.ReactionType;
import com.snackoverflow.reaction.repository.ReactionRepository;
import com.snackoverflow.recommendation.entity.SnackRecommendation;
import com.snackoverflow.recommendation.repository.SnackRecommendationRepository;
import com.snackoverflow.snack.entity.SnackPurchase;
import com.snackoverflow.snack.entity.SnackPurchaseStatus;
import com.snackoverflow.snack.repository.SnackPurchaseRepository;
import com.snackoverflow.user.entity.User;
import com.snackoverflow.user.entity.UserRole;
import com.snackoverflow.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReactionService 단위 테스트")
class ReactionServiceTest {

    @Mock ReactionRepository reactionRepository;
    @Mock SnackPurchaseRepository snackPurchaseRepository;
    @Mock SnackRecommendationRepository snackRecommendationRepository;
    @Mock CommentRepository commentRepository;

    @InjectMocks ReactionService service;

    private User user;
    private UUID userId;
    private UUID targetId;
    private SnackPurchase snack;

    @BeforeEach
    void setUp() {
        userId   = UUID.randomUUID();
        targetId = UUID.randomUUID();

        user = User.builder()
                .id(userId).username("user1").nickname("유저1")
                .email("u@test.com").passwordHash("hash")
                .role(UserRole.USER).status(UserStatus.ACTIVE)
                .build();
        snack = SnackPurchase.builder()
                .id(targetId).name("새우깡")
                .createdBy(user)
                .build();
    }

    @Test
    @DisplayName("toggle: 처음 좋아요를 누르면 likes 카운트가 1 증가한다")
    void toggle_firstLikeIncrementsCount() {
        given(reactionRepository.findByUserIdAndTargetTypeAndTargetId(userId, ReactionTargetType.SNACK_PURCHASE, targetId))
                .willReturn(Optional.empty())
                .willReturn(Optional.empty());
        given(snackPurchaseRepository.findById(targetId)).willReturn(Optional.of(snack));
        given(reactionRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        var result = service.toggle(user, ReactionTargetType.SNACK_PURCHASE, targetId, ReactionType.LIKE);

        assertThat(result.likes()).isEqualTo(1);
        assertThat(result.dislikes()).isEqualTo(0);
        then(reactionRepository).should().save(any(Reaction.class));
    }

    @Test
    @DisplayName("toggle: 같은 타입을 다시 누르면 반응이 취소된다")
    void toggle_sameLikeCancelsReaction() {
        var existing = Reaction.builder()
                .id(UUID.randomUUID()).user(user)
                .targetType(ReactionTargetType.SNACK_PURCHASE).targetId(targetId)
                .type(ReactionType.LIKE).build();
        snack.adjustLikes(1);

        given(reactionRepository.findByUserIdAndTargetTypeAndTargetId(userId, ReactionTargetType.SNACK_PURCHASE, targetId))
                .willReturn(Optional.of(existing))
                .willReturn(Optional.empty());
        given(snackPurchaseRepository.findById(targetId)).willReturn(Optional.of(snack));

        var result = service.toggle(user, ReactionTargetType.SNACK_PURCHASE, targetId, ReactionType.LIKE);

        assertThat(result.likes()).isEqualTo(0);
        assertThat(result.myReaction()).isNull();
        then(reactionRepository).should().delete(existing);
    }

    @Test
    @DisplayName("toggle: 좋아요 상태에서 싫어요 누르면 타입이 전환된다")
    void toggle_likeToDislikeSwitchesType() {
        var existing = Reaction.builder()
                .id(UUID.randomUUID()).user(user)
                .targetType(ReactionTargetType.SNACK_PURCHASE).targetId(targetId)
                .type(ReactionType.LIKE).build();
        snack.adjustLikes(1);

        given(reactionRepository.findByUserIdAndTargetTypeAndTargetId(userId, ReactionTargetType.SNACK_PURCHASE, targetId))
                .willReturn(Optional.of(existing))
                .willReturn(Optional.of(
                        Reaction.builder().id(existing.getId()).user(user)
                                .targetType(ReactionTargetType.SNACK_PURCHASE).targetId(targetId)
                                .type(ReactionType.DISLIKE).build()
                ));
        given(snackPurchaseRepository.findById(targetId)).willReturn(Optional.of(snack));

        var result = service.toggle(user, ReactionTargetType.SNACK_PURCHASE, targetId, ReactionType.DISLIKE);

        assertThat(result.likes()).isEqualTo(0);
        assertThat(result.dislikes()).isEqualTo(1);
        assertThat(result.myReaction()).isEqualTo("DISLIKE");
    }
}