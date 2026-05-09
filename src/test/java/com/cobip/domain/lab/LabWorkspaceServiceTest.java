package com.cobip.domain.lab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRole;
import com.cobip.domain.user.UserStatus;
import com.cobip.dto.lab.LabWorkspaceSaveRequest;
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
class LabWorkspaceServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private LabWorkspaceRepository labWorkspaceRepository;

    private LabWorkspaceService labWorkspaceService;

    @BeforeEach
    void setUp() {
        labWorkspaceService = new LabWorkspaceService(labWorkspaceRepository);
    }

    @Test
    void saveWorkspaceCreatesNewWorkspace() {
        User user = user();
        LabWorkspaceSaveRequest request = request("Java Variables", "JAVA", "src/Main.java");
        when(labWorkspaceRepository.findByUserIdAndWorkspaceKey(1L, "grammar-java-variables"))
                .thenReturn(Optional.empty());
        when(labWorkspaceRepository.save(any(LabWorkspace.class))).thenAnswer(invocation -> invocation.getArgument(0));

        labWorkspaceService.saveWorkspace(user, "grammar-java-variables", request);

        ArgumentCaptor<LabWorkspace> workspaceCaptor = ArgumentCaptor.forClass(LabWorkspace.class);
        verify(labWorkspaceRepository).save(workspaceCaptor.capture());
        assertThat(workspaceCaptor.getValue().getWorkspaceKey()).isEqualTo("grammar-java-variables");
        assertThat(workspaceCaptor.getValue().getLanguage()).isEqualTo("JAVA");
        assertThat(workspaceCaptor.getValue().getFilesJson().isArray()).isTrue();
    }

    @Test
    void saveWorkspaceUpdatesExistingWorkspace() {
        User user = user();
        LabWorkspace workspace = workspace(user);
        LabWorkspaceSaveRequest request = request("Updated", "PYTHON", "main.py");
        when(labWorkspaceRepository.findByUserIdAndWorkspaceKey(1L, "practice"))
                .thenReturn(Optional.of(workspace));
        when(labWorkspaceRepository.save(workspace)).thenReturn(workspace);

        labWorkspaceService.saveWorkspace(user, "practice", request);

        assertThat(workspace.getTitle()).isEqualTo("Updated");
        assertThat(workspace.getLanguage()).isEqualTo("PYTHON");
        assertThat(workspace.getActiveFilePath()).isEqualTo("main.py");
    }

    @Test
    void getWorkspaceRejectsMissingWorkspace() {
        User user = user();
        when(labWorkspaceRepository.findByUserIdAndWorkspaceKey(1L, "missing"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> labWorkspaceService.getWorkspace(user, "missing"))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.LAB_WORKSPACE_NOT_FOUND);
    }

    private LabWorkspaceSaveRequest request(String title, String language, String activeFilePath) {
        LabWorkspaceSaveRequest request = new LabWorkspaceSaveRequest();
        ReflectionTestUtils.setField(request, "title", title);
        ReflectionTestUtils.setField(request, "language", language);
        ReflectionTestUtils.setField(request, "activeFilePath", activeFilePath);
        ReflectionTestUtils.setField(request, "files", files());
        return request;
    }

    private JsonNode files() {
        return objectMapper.createArrayNode()
                .add(objectMapper.createObjectNode()
                        .put("path", "src/Main.java")
                        .put("content", "public class Main {}"));
    }

    private LabWorkspace workspace(User user) {
        return LabWorkspace.builder()
                .id(1L)
                .user(user)
                .workspaceKey("practice")
                .title("Practice")
                .language("JAVA")
                .activeFilePath("src/Main.java")
                .filesJson(files())
                .lastOpenedAt(LocalDateTime.now())
                .build();
    }

    private User user() {
        return User.builder()
                .id(1L)
                .email("user@example.com")
                .password("encoded-password")
                .nickname("user")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
    }
}
