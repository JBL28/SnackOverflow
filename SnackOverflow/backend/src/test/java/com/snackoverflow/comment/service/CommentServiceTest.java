package com.snackoverflow.comment.service;

import com.snackoverflow.comment.dto.CreateCommentRequest;
import com.snackoverflow.comment.dto.UpdateCommentRequest;
import com.snackoverflow.comment.entity.Comment;
import com.snackoverflow.comment.entity.TargetType;
import com.snackoverflow.comment.repository.CommentRepository;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService 단위 테스트")
class CommentServiceTest {

    @Mock CommentRepository commentRepository;
    @Mock UserRepository userRepository;

    @InjectMocks CommentService service;

    private User author;
    private User other;
    private UUID authorId;
    private UUID otherId;
    private UUID commentId;
    private Comment comment;

    @BeforeEach
    void setUp() {
        authorId  = UUID.randomUUID();
        otherId   = UUID.randomUUID();
        commentId = UUID.randomUUID();

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
        comment = Comment.builder()
                .id(commentId).content("맛있어요")
                .author(author)
                .targetType(TargetType.SNACK_PURCHASE)
                .targetId(UUID.randomUUID())
                .build();
    }

    @Test
    @DisplayName("create: 인증 사용자가 댓글을 작성할 수 있다")
    void create_savesCommentWithAuthor() {
        given(userRepository.findById(authorId)).willReturn(Optional.of(author));
        given(commentRepository.save(any())).willReturn(comment);

        var req = new CreateCommentRequest("맛있어요", TargetType.SNACK_PURCHASE, UUID.randomUUID(), null);
        var result = service.create(authorId, req);

        assertThat(result.content()).isEqualTo("맛있어요");
        assertThat(result.authorNickname()).isEqualTo("작성자");
    }

    @Test
    @DisplayName("update: 작성자 본인은 댓글을 수정할 수 있다")
    void update_ownerCanUpdate() {
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        var result = service.update(commentId, authorId, false, new UpdateCommentRequest("수정됨"));

        assertThat(result.content()).isEqualTo("수정됨");
    }

    @Test
    @DisplayName("update: 관리자는 타인 댓글도 수정할 수 있다")
    void update_adminCanUpdateOthers() {
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        var result = service.update(commentId, otherId, true, new UpdateCommentRequest("관리자수정"));

        assertThat(result.content()).isEqualTo("관리자수정");
    }

    @Test
    @DisplayName("update: 타인은 댓글을 수정할 수 없다")
    void update_nonOwnerThrowsSecurityException() {
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> service.update(commentId, otherId, false, new UpdateCommentRequest("x")))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("권한");
    }

    @Test
    @DisplayName("delete: 작성자 본인은 댓글을 삭제할 수 있다")
    void delete_ownerCanDelete() {
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        service.delete(commentId, authorId, false);

        then(commentRepository).should().delete(comment);
    }

    @Test
    @DisplayName("delete: 타인은 댓글을 삭제할 수 없다")
    void delete_nonOwnerThrowsSecurityException() {
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> service.delete(commentId, otherId, false))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("권한");
    }
}