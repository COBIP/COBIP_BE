package com.cobip.web.lab;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobip.domain.lab.LabWorkspaceService;
import com.cobip.domain.user.User;
import com.cobip.dto.lab.LabWorkspaceSaveRequest;
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

@WebMvcTest(LabWorkspaceController.class)
@AutoConfigureMockMvc(addFilters = false)
class LabWorkspaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LabWorkspaceService labWorkspaceService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void saveWorkspaceAcceptsRequestBody() throws Exception {
        when(labWorkspaceService.saveWorkspace(
                isNull(User.class),
                eq("practice"),
                any(LabWorkspaceSaveRequest.class)
        )).thenReturn(null);

        mockMvc.perform(put("/api/v1/lab/workspaces/practice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "Java Practice",
                              "language": "JAVA",
                              "activeFilePath": "src/Main.java",
                              "files": [
                                {
                                  "path": "src/Main.java",
                                  "content": "public class Main {}"
                                }
                              ]
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void workspaceQueryEndpointsReturnSuccessResponse() throws Exception {
        when(labWorkspaceService.getWorkspace(isNull(User.class), eq("practice"))).thenReturn(null);
        when(labWorkspaceService.getWorkspaces(isNull(User.class), any(Pageable.class)))
                .thenReturn(PageResponse.from(Page.empty()));

        mockMvc.perform(get("/api/v1/lab/workspaces/practice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/lab/workspaces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void deleteWorkspaceReturnsSuccessResponse() throws Exception {
        doNothing().when(labWorkspaceService).deleteWorkspace(isNull(User.class), eq("practice"));

        mockMvc.perform(delete("/api/v1/lab/workspaces/practice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
