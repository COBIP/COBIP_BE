package com.cobip.web.practice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobip.domain.practice.TemplatePracticeService;
import com.cobip.domain.user.User;
import com.cobip.dto.practice.TemplatePracticeMissionProgressUpdateRequest;
import com.cobip.global.security.JwtAuthenticationFilter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TemplatePracticeController.class)
@AutoConfigureMockMvc(addFilters = false)
class TemplatePracticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TemplatePracticeService templatePracticeService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void getPracticeReturnsSuccessResponse() throws Exception {
        when(templatePracticeService.getPractice(isNull(User.class), eq(1L))).thenReturn(null);

        mockMvc.perform(get("/api/v1/templates/1/practice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void startPracticeReturnsSuccessResponse() throws Exception {
        when(templatePracticeService.startPractice(isNull(User.class), eq(1L))).thenReturn(null);

        mockMvc.perform(post("/api/v1/templates/1/practice/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateMissionProgressAcceptsPatchRequest() throws Exception {
        when(templatePracticeService.updateMissionProgress(
                isNull(User.class),
                eq(1L),
                eq(2L),
                any(TemplatePracticeMissionProgressUpdateRequest.class)
        )).thenReturn(null);

        mockMvc.perform(patch("/api/v1/templates/1/practice/missions/2/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "status": "COMPLETED"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
