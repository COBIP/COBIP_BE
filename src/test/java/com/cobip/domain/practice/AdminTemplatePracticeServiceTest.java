package com.cobip.domain.practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.cobip.domain.template.Template;
import com.cobip.domain.template.TemplateAccessLevel;
import com.cobip.domain.template.TemplateDifficulty;
import com.cobip.domain.template.TemplateRepository;
import com.cobip.domain.template.TemplateVisibility;
import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRole;
import com.cobip.dto.practice.TemplatePracticeFileRequest;
import com.cobip.dto.practice.TemplatePracticeFileResponse;
import com.cobip.dto.practice.TemplatePracticeMissionRequest;
import com.cobip.dto.practice.TemplatePracticeMissionResponse;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminTemplatePracticeServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private TemplatePracticeFileRepository fileRepository;

    @Mock
    private TemplatePracticeMissionRepository missionRepository;

    private AdminTemplatePracticeService adminTemplatePracticeService;

    @BeforeEach
    void setUp() {
        adminTemplatePracticeService = new AdminTemplatePracticeService(
                templateRepository,
                fileRepository,
                missionRepository
        );
    }

    @Test
    void createFileSavesPracticeFile() {
        Template template = template();
        TemplatePracticeFileRequest request = fileRequest("src/main/java/com/example/AuthController.java");
        when(templateRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(template));
        when(fileRepository.save(any(TemplatePracticeFile.class))).thenAnswer(invocation -> {
            TemplatePracticeFile file = invocation.getArgument(0);
            ReflectionTestUtils.setField(file, "id", 10L);
            return file;
        });

        TemplatePracticeFileResponse response = adminTemplatePracticeService.createFile(1L, request);

        ArgumentCaptor<TemplatePracticeFile> fileCaptor = ArgumentCaptor.forClass(TemplatePracticeFile.class);
        verify(fileRepository).save(fileCaptor.capture());
        assertThat(fileCaptor.getValue().getTemplate()).isEqualTo(template);
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getFilePath()).isEqualTo("src/main/java/com/example/AuthController.java");
    }

    @Test
    void updateFileChangesPracticeFile() {
        Template template = template();
        TemplatePracticeFile file = file(template);
        TemplatePracticeFileRequest request = fileRequest("src/main/java/com/example/LoginController.java");
        when(templateRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(template));
        when(fileRepository.findByIdAndTemplateId(10L, 1L)).thenReturn(Optional.of(file));

        TemplatePracticeFileResponse response = adminTemplatePracticeService.updateFile(1L, 10L, request);

        assertThat(response.getFilePath()).isEqualTo("src/main/java/com/example/LoginController.java");
        assertThat(response.getLanguage()).isEqualTo("JAVA");
        assertThat(response.isReadOnly()).isFalse();
    }

    @Test
    void updateMissionChangesPracticeMission() {
        Template template = template();
        TemplatePracticeMission mission = mission(template);
        JsonNode validationJson = objectMapper.createObjectNode().put("testCommand", "./gradlew test");
        TemplatePracticeMissionRequest request = missionRequest("JWT login", validationJson);
        when(templateRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(template));
        when(missionRepository.findByIdAndTemplateId(20L, 1L)).thenReturn(Optional.of(mission));

        TemplatePracticeMissionResponse response = adminTemplatePracticeService.updateMission(1L, 20L, request);

        assertThat(response.getTitle()).isEqualTo("JWT login");
        assertThat(response.getMissionType()).isEqualTo(TemplatePracticeMissionType.IMPLEMENTATION);
        assertThat(response.getValidationJson()).isEqualTo(validationJson);
    }

    @Test
    void deleteFileRejectsMissingFile() {
        when(templateRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(template()));
        when(fileRepository.findByIdAndTemplateId(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminTemplatePracticeService.deleteFile(1L, 10L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TEMPLATE_PRACTICE_FILE_NOT_FOUND);
    }

    private TemplatePracticeFileRequest fileRequest(String filePath) {
        TemplatePracticeFileRequest request = new TemplatePracticeFileRequest();
        ReflectionTestUtils.setField(request, "filePath", filePath);
        ReflectionTestUtils.setField(request, "language", "JAVA");
        ReflectionTestUtils.setField(request, "content", "class AuthController {}");
        ReflectionTestUtils.setField(request, "readOnly", false);
        ReflectionTestUtils.setField(request, "orderIndex", 1);
        return request;
    }

    private TemplatePracticeMissionRequest missionRequest(String title, JsonNode validationJson) {
        TemplatePracticeMissionRequest request = new TemplatePracticeMissionRequest();
        ReflectionTestUtils.setField(request, "title", title);
        ReflectionTestUtils.setField(request, "description", "Implement login API");
        ReflectionTestUtils.setField(request, "missionType", TemplatePracticeMissionType.IMPLEMENTATION);
        ReflectionTestUtils.setField(request, "orderIndex", 1);
        ReflectionTestUtils.setField(request, "guideContent", "Use Spring Security");
        ReflectionTestUtils.setField(request, "validationJson", validationJson);
        return request;
    }

    private TemplatePracticeFile file(Template template) {
        return TemplatePracticeFile.builder()
                .id(10L)
                .template(template)
                .filePath("src/main/java/com/example/AuthController.java")
                .language("JAVA")
                .content("class AuthController {}")
                .readOnly(false)
                .orderIndex(1)
                .build();
    }

    private TemplatePracticeMission mission(Template template) {
        return TemplatePracticeMission.builder()
                .id(20L)
                .template(template)
                .title("Old mission")
                .description("Old description")
                .missionType(TemplatePracticeMissionType.CONCEPT)
                .orderIndex(1)
                .build();
    }

    private Template template() {
        return Template.builder()
                .id(1L)
                .owner(user())
                .title("JWT OAuth2 Login")
                .description("description")
                .category("backend")
                .difficulty(TemplateDifficulty.INTERMEDIATE)
                .techStacks(List.of("Spring", "JWT", "OAuth2"))
                .visibility(TemplateVisibility.PUBLIC)
                .accessLevel(TemplateAccessLevel.FREE)
                .build();
    }

    private User user() {
        return User.builder()
                .id(1L)
                .email("admin@example.com")
                .password("password")
                .nickname("admin")
                .role(UserRole.ADMIN)
                .emailVerified(true)
                .build();
    }
}
