package com.snackoverflow.recommendation.service;

import com.snackoverflow.recommendation.dto.CreateSnackRecommendationRequest;
import com.snackoverflow.recommendation.dto.UpdateSnackRecommendationRequest;
import com.snackoverflow.recommendation.entity.SnackRecommendation;
import com.snackoverflow.recommendation.repository.SnackRecommendationRepository;
import com.snackoverflow.user.entity.User;
import com.snackoverflow.user.entity.UserRole;
import com.snackoverflow.user.entity.UserStatus;
import com.snackoverflow.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("SnackRecommendationService 단위 테스트")
class SnackRecommendationServiceTest {

    @Mock SnackRecommendationRepository recommendationRepository;
    @Mock UserRepository userRepository;

    @InjectMocks SnackRecommendationService service;

    private User author;
    private User other;
    private UUID authorId;
    private UUID otherId;
    private UUID recId;
    private SnackRecommendation rec;

    @BeforeEach
    void setUp() {
        authorId = UUID.randomUUID();
        otherId  = UUID.randomUUID();
        recId    = UUID.randomUUID();

        author = User.builder()
                .id(authorId).username("author").nickname("작성자")
                .email("a@test.com").passwordHash("hash")
                .role(UserRole.USER).status(UserStatus.ACTIVE)
                .build();
        other = User.builder()
                .id(otherId).username("other").nickname("타인")
                .email("b@test.com").passwordHash("hash")
                .role(UserRole.USER).status(UserStatus.ACTIVE)
                .build();
        rec = SnackRecommendation.builder()
                .id(recId).name("허니버터칩").reason("달달함")
                .createdBy(author).build();
    }

    @Test
    @DisplayName("create: 작성자 정보로 추천 게시글을 생성한다")
    void create_savesRecommendationWithAuthor() {
        given(userRepository.findById(authorId)).willReturn(Optional.of(author));
        given(recommendationRepository.save(any())).willReturn(rec);

        var result = service.create(authorId, new CreateSnackRecommendationRequest("허니버터칩", "달달함"));

        assertThat(result.name()).isEqualTo("허니버터칩");
        assertThat(result.createdByNickname()).isEqualTo("작성자");
        then(recommendationRepository).should().save(any(SnackRecommendation.class));
    }

    @Test
    @DisplayName("update: 작성자 본인은 수정할 수 있다")
    void update_ownerCanUpdate() {
        given(recommendationRepository.findById(recId)).willReturn(Optional.of(rec));

        var result = service.update(recId, authorId, false, new UpdateSnackRecommendationRequest("새이름", "새이유"));

        assertThat(result.name()).isEqualTo("새이름");
    }

    @Test
    @DisplayName("update: 관리자는 타인 게시글도 수정할 수 있다")
    void update_adminCanUpdateOthers() {
        given(recommendationRepository.findById(recId)).willReturn(Optional.of(rec));

        var result = service.update(recId, otherId, true, new UpdateSnackRecommendationRequest("관리자수정", "이유"));

        assertThat(result.name()).isEqualTo("관리자수정");
    }

    @Test
    @DisplayName("update: 다른 사용자는 수정할 수 없다")
    void update_nonOwnerThrowsSecurityException() {
        given(recommendationRepository.findById(recId)).willReturn(Optional.of(rec));

        assertThatThrownBy(() -> service.update(recId, otherId, false,
                new UpdateSnackRecommendationRequest("x", "y")))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("권한");
    }

    @Test
    @DisplayName("delete: 작성자 본인은 삭제할 수 있다")
    void delete_ownerCanDelete() {
        given(recommendationRepository.findById(recId)).willReturn(Optional.of(rec));

        service.delete(recId, authorId, false);

        then(recommendationRepository).should().delete(rec);
    }

    @Test
    @DisplayName("delete: 다른 사용자는 삭제할 수 없다")
    void delete_nonOwnerThrowsSecurityException() {
        given(recommendationRepository.findById(recId)).willReturn(Optional.of(rec));

        assertThatThrownBy(() -> service.delete(recId, otherId, false))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("권한");
    }

    @Test
    @DisplayName("getOne: 존재하지 않는 ID면 예외를 던진다")
    void getOne_notFoundThrows() {
        given(recommendationRepository.findById(any())).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOne(UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}