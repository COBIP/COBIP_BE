package com.cobip.web.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobip.domain.practice.AdminTemplatePracticeService;
import com.cobip.dto.practice.TemplatePracticeFileRequest;
import com.cobip.dto.practice.TemplatePracticeMissionRequest;
import com.cobip.global.security.JwtAuthenticationFilter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminTemplatePracticeController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminTemplatePracticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminTemplatePracticeService adminTemplatePracticeService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void getPracticeReturnsPracticeDetailEnvelope() throws Exception {
        when(adminTemplatePracticeService.getPractice(1L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/admin/templates/1/practice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createFileAcceptsRequest() throws Exception {
        when(adminTemplatePracticeService.createFile(eq(1L), any(TemplatePracticeFileRequest.class)))
                .thenReturn(null);

        mockMvc.perform(post("/api/v1/admin/templates/1/practice/files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "filePath": "src/main/java/com/example/AuthController.java",
                              "language": "JAVA",
                              "content": "class AuthController {}",
                              "readOnly": false,
                              "orderIndex": 1
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateFileRejectsMissingFilePath() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/templates/1/practice/files/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "language": "JAVA",
                              "content": "class AuthController {}",
                              "readOnly": false,
                              "orderIndex": 1
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createMissionAcceptsRequest() throws Exception {
        when(adminTemplatePracticeService.createMission(eq(1L), any(TemplatePracticeMissionRequest.class)))
                .thenReturn(null);

        mockMvc.perform(post("/api/v1/admin/templates/1/practice/missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "JWT login",
                              "description": "Implement login API",
                              "missionType": "IMPLEMENTATION",
                              "orderIndex": 1,
                              "guideContent": "Use Spring Security",
                              "validationJson": {
                                "testCommand": "./gradlew test"
                              }
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateMissionAcceptsRequest() throws Exception {
        when(adminTemplatePracticeService.updateMission(
                eq(1L),
                eq(20L),
                any(TemplatePracticeMissionRequest.class)
        )).thenReturn(null);

        mockMvc.perform(patch("/api/v1/admin/templates/1/practice/missions/20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "JWT login",
                              "description": "Implement login API",
                              "missionType": "IMPLEMENTATION",
                              "orderIndex": 1
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteMissionAcceptsRequest() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/templates/1/practice/missions/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
