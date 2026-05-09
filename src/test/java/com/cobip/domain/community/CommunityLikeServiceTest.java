package com.cobip.domain.community;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRepository;
import com.cobip.domain.user.UserRole;
import com.cobip.domain.user.UserStatus;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommunityLikeServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CommunityPostRepository communityPostRepository;

    @Mock
    private CommunityCommentRepository communityCommentRepository;

    @Mock
    private CommunityLikeRepository communityLikeRepository;

    @Mock
    private UserRepository userRepository;

    private CommunityLikeService communityLikeService;

    @BeforeEach
    void setUp() {
        communityLikeService = new CommunityLikeService(
                communityPostRepository,
                communityCommentRepository,
                communityLikeRepository,
                userRepository
        );
    }

    @Test
    void addPostLikeStoresLikeAndIncreasesCount() {
        User user = user(1L);
        CommunityPost post = post(user);
        when(communityPostRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(post));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        communityLikeService.addPostLike(user, 1L);

        ArgumentCaptor<CommunityLike> likeCaptor = ArgumentCaptor.forClass(CommunityLike.class);
        verify(communityLikeRepository).save(likeCaptor.capture());
        assertThat(likeCaptor.getValue().getTargetType()).isEqualTo(CommunityLikeTargetType.POST);
        assertThat(likeCaptor.getValue().getTargetId()).isEqualTo(1L);
        assertThat(post.getLikeCount()).isEqualTo(1);
    }

    @Test
    void addPostLikeRejectsDuplicatedLike() {
        User user = user(1L);
        CommunityPost post = post(user);
        when(communityPostRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(post));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(communityLikeRepository.existsByUserIdAndTargetTypeAndTargetId(1L, CommunityLikeTargetType.POST, 1L))
                .thenReturn(true);

        assertThatThrownBy(() -> communityLikeService.addPostLike(user, 1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_COMMUNITY_LIKE);
    }

    @Test
    void removeCommentLikeDeletesLikeAndDecreasesCount() {
        User user = user(1L);
        CommunityPost post = post(user);
        CommunityComment comment = comment(post, user);
        comment.increaseLikeCount();
        CommunityLike like = CommunityLike.create(user, CommunityLikeTargetType.COMMENT, 1L);
        when(communityCommentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(comment));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(communityLikeRepository.findByUserIdAndTargetTypeAndTargetId(1L, CommunityLikeTargetType.COMMENT, 1L))
                .thenReturn(Optional.of(like));

        communityLikeService.removeCommentLike(user, 1L);

        verify(communityLikeRepository).delete(like);
        assertThat(comment.getLikeCount()).isZero();
    }

    private CommunityComment comment(CommunityPost post, User author) {
        return CommunityComment.builder()
                .id(1L)
                .post(post)
                .author(author)
                .content("comment")
                .status(CommunityCommentStatus.VISIBLE)
                .likeCount(0)
                .build();
    }

    private CommunityPost post(User author) {
        return CommunityPost.builder()
                .id(1L)
                .author(author)
                .category(CommunityPostCategory.GENERAL)
                .title("title")
                .contentJson(objectMapper.createObjectNode())
                .searchableText("body")
                .status(CommunityPostStatus.VISIBLE)
                .viewCount(0)
                .commentCount(0)
                .likeCount(0)
                .build();
    }

    private User user(Long id) {
        return User.builder()
                .id(id)
                .email("user" + id + "@example.com")
                .password("encoded-password")
                .nickname("user" + id)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
    }
}
