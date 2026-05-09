package com.cobip.web.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobip.domain.user.User;
import com.cobip.domain.user.UserSettingService;
import com.cobip.dto.user.UserSettingUpdateRequest;
import com.cobip.global.security.JwtAuthenticationFilter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserSettingController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserSettingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserSettingService userSettingService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void getSettingsReturnsResponseEnvelope() throws Exception {
        when(userSettingService.getSettings(nullable(User.class))).thenReturn(null);

        mockMvc.perform(get("/api/v1/users/me/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateSettingsAcceptsRequestBody() throws Exception {
        when(userSettingService.updateSettings(nullable(User.class), any(UserSettingUpdateRequest.class))).thenReturn(null);

        mockMvc.perform(patch("/api/v1/users/me/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "pushNotificationEnabled": false,
                              "emailNotificationEnabled": true,
                              "editorFontSize": 18,
                              "editorTheme": "DARK",
                              "editorTabSize": 2,
                              "serviceTheme": "DARK",
                              "autoLoginEnabled": true
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateSettingsRejectsInvalidEditorFontSize() throws Exception {
        mockMvc.perform(patch("/api/v1/users/me/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "editorFontSize": 100
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
