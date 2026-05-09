package com.cobip.domain.community;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRepository;
import com.cobip.domain.user.UserRole;
import com.cobip.domain.user.UserStatus;
import com.cobip.dto.community.CommunityPostSummaryResponse;
import com.cobip.global.common.PageResponse;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class CommunityPostSaveServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CommunityPostRepository communityPostRepository;

    @Mock
    private CommunityPostSaveRepository communityPostSaveRepository;

    @Mock
    private UserRepository userRepository;

    private CommunityPostSaveService communityPostSaveService;

    @BeforeEach
    void setUp() {
        communityPostSaveService = new CommunityPostSaveService(
                communityPostRepository,
                communityPostSaveRepository,
                userRepository
        );
    }

    @Test
    void savePostStoresCommunityPostSave() {
        User user = user(1L);
        CommunityPost post = post(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(communityPostRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(post));

        communityPostSaveService.savePost(user, 1L);

        ArgumentCaptor<CommunityPostSave> saveCaptor = ArgumentCaptor.forClass(CommunityPostSave.class);
        verify(communityPostSaveRepository).save(saveCaptor.capture());
        assertThat(saveCaptor.getValue().getUser()).isEqualTo(user);
        assertThat(saveCaptor.getValue().getPost()).isEqualTo(post);
    }

    @Test
    void savePostRejectsDuplicatedSave() {
        User user = user(1L);
        CommunityPost post = post(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(communityPostRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(post));
        when(communityPostSaveRepository.existsByUserIdAndPostId(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> communityPostSaveService.savePost(user, 1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_COMMUNITY_POST_SAVE);
    }

    @Test
    void unsavePostDeletesExistingSave() {
        User user = user(1L);
        CommunityPost post = post(user);
        CommunityPostSave save = CommunityPostSave.create(user, post);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(communityPostRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(post));
        when(communityPostSaveRepository.findByUserIdAndPostId(1L, 1L)).thenReturn(Optional.of(save));

        communityPostSaveService.unsavePost(user, 1L);

        verify(communityPostSaveRepository).delete(save);
    }

    @Test
    void getSavedPostsReturnsVisibleSavedPosts() {
        User user = user(1L);
        CommunityPostSave save = CommunityPostSave.create(user, post(user));
        PageRequest pageable = PageRequest.of(0, 20);
        when(communityPostSaveRepository.findVisibleSavedPosts(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(save), pageable, 1));

        PageResponse<CommunityPostSummaryResponse> response = communityPostSaveService.getSavedPosts(user, pageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getId()).isEqualTo(1L);
        assertThat(response.getContent().get(0).getTitle()).isEqualTo("title");
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
