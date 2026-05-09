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

import com.cobip.domain.community.CommunityCommentService;
import com.cobip.domain.user.User;
import com.cobip.dto.community.CommunityCommentCreateRequest;
import com.cobip.dto.community.CommunityCommentUpdateRequest;
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

@WebMvcTest(CommunityCommentController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommunityCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommunityCommentService communityCommentService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void getCommentsReturnsPagedResponse() throws Exception {
        when(communityCommentService.getComments(eq(1L), any(Pageable.class)))
                .thenReturn(PageResponse.from(Page.empty()));

        mockMvc.perform(get("/api/v1/community/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void createCommentAcceptsRequestBody() throws Exception {
        when(communityCommentService.createComment(
                isNull(User.class),
                eq(1L),
                any(CommunityCommentCreateRequest.class)
        )).thenReturn(null);

        mockMvc.perform(post("/api/v1/community/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "content": "댓글입니다."
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getRepliesReturnsPagedResponse() throws Exception {
        when(communityCommentService.getReplies(eq(1L), any(Pageable.class)))
                .thenReturn(PageResponse.from(Page.empty()));

        mockMvc.perform(get("/api/v1/community/comments/1/replies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void updateCommentAcceptsPatchRequest() throws Exception {
        when(communityCommentService.updateComment(
                isNull(User.class),
                eq(1L),
                any(CommunityCommentUpdateRequest.class)
        )).thenReturn(null);

        mockMvc.perform(patch("/api/v1/community/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "content": "수정된 댓글입니다."
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteCommentReturnsSuccessEnvelope() throws Exception {
        doNothing().when(communityCommentService).deleteComment(isNull(User.class), eq(1L));

        mockMvc.perform(delete("/api/v1/community/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
