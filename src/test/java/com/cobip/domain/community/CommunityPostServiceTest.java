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
import com.cobip.dto.community.CommunityPostCreateRequest;
import com.cobip.dto.community.CommunityPostDetailResponse;
import com.cobip.dto.community.CommunityPostUpdateRequest;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CommunityPostServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CommunityPostRepository communityPostRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommunityPostTextExtractor textExtractor;

    private CommunityPostService communityPostService;

    @BeforeEach
    void setUp() {
        communityPostService = new CommunityPostService(
                communityPostRepository,
                userRepository,
                textExtractor
        );
    }

    @Test
    void createPostStoresSearchableText() {
        User author = user(1L);
        CommunityPostCreateRequest request = createRequest("title", contentJson());
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(textExtractor.extract(request.getContentJson())).thenReturn("body text");
        when(communityPostRepository.save(any(CommunityPost.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommunityPostDetailResponse response = communityPostService.createPost(author, request);

        ArgumentCaptor<CommunityPost> postCaptor = ArgumentCaptor.forClass(CommunityPost.class);
        verify(communityPostRepository).save(postCaptor.capture());
        assertThat(postCaptor.getValue().getSearchableText()).isEqualTo("body text");
        assertThat(postCaptor.getValue().getStatus()).isEqualTo(CommunityPostStatus.VISIBLE);
        assertThat(response.getTitle()).isEqualTo("title");
    }

    @Test
    void getPostIncreasesViewCount() {
        CommunityPost post = post(user(1L));
        when(communityPostRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(post));

        CommunityPostDetailResponse response = communityPostService.getPost(1L);

        assertThat(post.getViewCount()).isEqualTo(1);
        assertThat(response.getViewCount()).isEqualTo(1);
    }

    @Test
    void updatePostRejectsNonAuthor() {
        CommunityPost post = post(user(2L));
        when(communityPostRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> communityPostService.updatePost(user(1L), 1L, updateRequest("updated")))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    @Test
    void deletePostMarksPostDeleted() {
        User author = user(1L);
        CommunityPost post = post(author);
        when(communityPostRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(post));

        communityPostService.deletePost(author, 1L);

        assertThat(post.getDeletedAt()).isNotNull();
    }

    @Test
    void createPostRejectsScalarContentJson() throws Exception {
        CommunityPostCreateRequest request = createRequest("title", objectMapper.readTree("\"text\""));

        assertThatThrownBy(() -> communityPostService.createPost(user(1L), request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    private CommunityPostCreateRequest createRequest(String title, JsonNode contentJson) {
        CommunityPostCreateRequest request = new CommunityPostCreateRequest();
        ReflectionTestUtils.setField(request, "category", CommunityPostCategory.GENERAL);
        ReflectionTestUtils.setField(request, "title", title);
        ReflectionTestUtils.setField(request, "contentJson", contentJson);
        return request;
    }

    private CommunityPostUpdateRequest updateRequest(String title) {
        CommunityPostUpdateRequest request = new CommunityPostUpdateRequest();
        ReflectionTestUtils.setField(request, "title", title);
        return request;
    }

    private JsonNode contentJson() {
        return objectMapper.createObjectNode()
                .put("type", "doc")
                .set("content", objectMapper.createArrayNode());
    }

    private CommunityPost post(User author) {
        return CommunityPost.builder()
                .id(1L)
                .author(author)
                .category(CommunityPostCategory.GENERAL)
                .title("title")
                .contentJson(contentJson())
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
