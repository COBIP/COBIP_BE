package com.cobip.web.community;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobip.domain.community.CommunityPostCategory;
import com.cobip.domain.community.CommunityPostService;
import com.cobip.domain.community.CommunityPostSort;
import com.cobip.domain.user.User;
import com.cobip.dto.community.CommunityPostCreateRequest;
import com.cobip.dto.community.CommunityPostUpdateRequest;
import com.cobip.global.common.PageResponse;
import com.cobip.global.security.JwtAuthenticationFilter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CommunityPostController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommunityPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommunityPostService communityPostService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void getPostsReturnsPagedResponse() throws Exception {
        when(communityPostService.getPosts(
                eq("spring"),
                eq(CommunityPostCategory.QNA),
                eq(CommunityPostSort.POPULAR),
                any(Pageable.class)
        )).thenReturn(PageResponse.from(Page.empty()));

        mockMvc.perform(get("/api/v1/community/posts")
                        .param("keyword", "spring")
                        .param("category", "QNA")
                        .param("sort", "POPULAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void getPostReturnsSuccessEnvelope() throws Exception {
        when(communityPostService.getPost(1L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/community/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createPostAcceptsRequestBody() throws Exception {
        when(communityPostService.createPost(isNull(User.class), any(CommunityPostCreateRequest.class))).thenReturn(null);

        mockMvc.perform(post("/api/v1/community/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "category": "GENERAL",
                              "title": "첫 게시글",
                              "contentJson": {
                                "type": "doc",
                                "content": []
                              }
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updatePostAcceptsPatchRequest() throws Exception {
        when(communityPostService.updatePost(
                isNull(User.class),
                eq(1L),
                any(CommunityPostUpdateRequest.class)
        )).thenReturn(null);

        mockMvc.perform(patch("/api/v1/community/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "수정된 게시글"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deletePostReturnsSuccessEnvelope() throws Exception {
        doNothing().when(communityPostService).deletePost(isNull(User.class), eq(1L));

        mockMvc.perform(delete("/api/v1/community/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
