package com.cobip.web.community;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobip.domain.community.CommunityLikeService;
import com.cobip.domain.user.User;
import com.cobip.global.security.JwtAuthenticationFilter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CommunityLikeController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommunityLikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommunityLikeService communityLikeService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void postLikeEndpointsReturnSuccessResponse() throws Exception {
        doNothing().when(communityLikeService).addPostLike(isNull(User.class), eq(1L));
        doNothing().when(communityLikeService).removePostLike(isNull(User.class), eq(1L));

        mockMvc.perform(post("/api/v1/community/posts/1/like"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(delete("/api/v1/community/posts/1/like"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void commentLikeEndpointsReturnSuccessResponse() throws Exception {
        doNothing().when(communityLikeService).addCommentLike(isNull(User.class), eq(1L));
        doNothing().when(communityLikeService).removeCommentLike(isNull(User.class), eq(1L));

        mockMvc.perform(post("/api/v1/community/comments/1/like"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(delete("/api/v1/community/comments/1/like"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
