package com.cobip.web.lab;

import com.cobip.domain.lab.LabWorkspaceService;
import com.cobip.domain.user.User;
import com.cobip.dto.lab.LabWorkspaceResponse;
import com.cobip.dto.lab.LabWorkspaceSaveRequest;
import com.cobip.global.common.ApiResponse;
import com.cobip.global.common.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lab/workspaces")
public class LabWorkspaceController {

    private final LabWorkspaceService labWorkspaceService;

    @PutMapping("/{workspaceKey}")
    public ResponseEntity<ApiResponse<LabWorkspaceResponse>> saveWorkspace(
        @AuthenticationPrincipal User user,
        @PathVariable String workspaceKey,
        @RequestBody @Valid LabWorkspaceSaveRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(labWorkspaceService.saveWorkspace(user, workspaceKey, request)));
    }

    @GetMapping("/{workspaceKey}")
    public ResponseEntity<ApiResponse<LabWorkspaceResponse>> getWorkspace(
        @AuthenticationPrincipal User user,
        @PathVariable String workspaceKey
    ) {
        return ResponseEntity.ok(ApiResponse.success(labWorkspaceService.getWorkspace(user, workspaceKey)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LabWorkspaceResponse>>> getWorkspaces(
        @AuthenticationPrincipal User user,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(labWorkspaceService.getWorkspaces(user, pageable)));
    }

    @DeleteMapping("/{workspaceKey}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkspace(
        @AuthenticationPrincipal User user,
        @PathVariable String workspaceKey
    ) {
        labWorkspaceService.deleteWorkspace(user, workspaceKey);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
