package com.cobip.web.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobip.domain.moderation.AdminContentModerationService;
import com.cobip.domain.user.User;
import com.cobip.dto.admin.AdminContentModerationRequest;
import com.cobip.global.security.JwtAuthenticationFilter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminContentModerationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminContentModerationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminContentModerationService adminContentModerationService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void moderateAcceptsRequestBody() throws Exception {
        when(adminContentModerationService.moderate(
                any(AdminContentModerationRequest.class),
                isNull(User.class)
        )).thenReturn(null);

        mockMvc.perform(post("/api/v1/admin/content-moderations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "targetType": "TEMPLATE",
                              "targetId": 1,
                              "action": "BLIND",
                              "adminMemo": "Inappropriate content"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void moderateRejectsMissingAction() throws Exception {
        mockMvc.perform(post("/api/v1/admin/content-moderations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "targetType": "TEMPLATE",
                              "targetId": 1
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
