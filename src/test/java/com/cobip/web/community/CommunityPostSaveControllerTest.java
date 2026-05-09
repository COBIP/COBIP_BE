package com.cobip.web.community;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobip.domain.community.CommunityPostSaveService;
import com.cobip.domain.user.User;
import com.cobip.global.common.PageResponse;
import com.cobip.global.security.JwtAuthenticationFilter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CommunityPostSaveController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommunityPostSaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommunityPostSaveService communityPostSaveService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void saveEndpointsReturnSuccessResponse() throws Exception {
        doNothing().when(communityPostSaveService).savePost(isNull(User.class), eq(1L));
        doNothing().when(communityPostSaveService).unsavePost(isNull(User.class), eq(1L));

        mockMvc.perform(post("/api/v1/community/posts/1/save"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(delete("/api/v1/community/posts/1/save"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void savedPostsEndpointReturnsPagedResponse() throws Exception {
        when(communityPostSaveService.getSavedPosts(isNull(User.class), any(Pageable.class)))
                .thenReturn(PageResponse.from(Page.empty()));

        mockMvc.perform(get("/api/v1/community/posts/saved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }
}
