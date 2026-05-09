package com.cobip.domain.community;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRepository;
import com.cobip.domain.user.UserRole;
import com.cobip.domain.user.UserStatus;
import com.cobip.dto.community.CommunityCommentCreateRequest;
import com.cobip.dto.community.CommunityCommentResponse;
import com.cobip.dto.community.CommunityCommentUpdateRequest;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CommunityCommentServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CommunityPostRepository communityPostRepository;

    @Mock
    private CommunityCommentRepository communityCommentRepository;

    @Mock
    private UserRepository userRepository;

    private CommunityCommentService communityCommentService;

    @BeforeEach
    void setUp() {
        communityCommentService = new CommunityCommentService(
                communityPostRepository,
                communityCommentRepository,
                userRepository
        );
    }

    @Test
    void createCommentIncreasesPostCommentCount() {
        User author = user(1L);
        CommunityPost post = post(author);
        CommunityCommentCreateRequest request = createRequest(null, " comment ");
        when(communityPostRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(post));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(communityCommentRepository.save(any(CommunityComment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommunityCommentResponse response = communityCommentService.createComment(author, 1L, request);

        assertThat(post.getCommentCount()).isEqualTo(1);
        assertThat(response.getContent()).isEqualTo("comment");
        assertThat(response.getParentCommentId()).isNull();
    }

    @Test
    void createReplyRejectsReplyAsParent() {
        User author = user(1L);
        CommunityPost post = post(author);
        CommunityComment parent = comment(10L, post, author, null);
        CommunityComment reply = comment(11L, post, author, parent);
        CommunityCommentCreateRequest request = createRequest(11L, "reply");
        when(communityPostRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(post));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(communityCommentRepository.findByIdAndDeletedAtIsNull(11L)).thenReturn(Optional.of(reply));

        assertThatThrownBy(() -> communityCommentService.createComment(author, 1L, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    void updateCommentRejectsNonAuthor() {
        CommunityPost post = post(user(2L));
        CommunityComment comment = comment(1L, post, user(2L), null);
        when(communityCommentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> communityCommentService.updateComment(user(1L), 1L, updateRequest("updated")))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    @Test
    void deleteCommentMarksDeletedAndDecreasesPostCommentCount() {
        User author = user(1L);
        CommunityPost post = post(author);
        post.increaseCommentCount();
        CommunityComment comment = comment(1L, post, author, null);
        when(communityCommentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(comment));

        communityCommentService.deleteComment(author, 1L);

        assertThat(comment.getDeletedAt()).isNotNull();
        assertThat(post.getCommentCount()).isZero();
    }

    private CommunityCommentCreateRequest createRequest(Long parentCommentId, String content) {
        CommunityCommentCreateRequest request = new CommunityCommentCreateRequest();
        ReflectionTestUtils.setField(request, "parentCommentId", parentCommentId);
        ReflectionTestUtils.setField(request, "content", content);
        return request;
    }

    private CommunityCommentUpdateRequest updateRequest(String content) {
        CommunityCommentUpdateRequest request = new CommunityCommentUpdateRequest();
        ReflectionTestUtils.setField(request, "content", content);
        return request;
    }

    private CommunityComment comment(Long id, CommunityPost post, User author, CommunityComment parentComment) {
        return CommunityComment.builder()
                .id(id)
                .post(post)
                .author(author)
                .parentComment(parentComment)
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
