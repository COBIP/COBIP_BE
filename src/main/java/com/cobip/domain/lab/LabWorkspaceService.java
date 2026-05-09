package com.cobip.domain.lab;

import com.cobip.domain.user.User;
import com.cobip.dto.lab.LabWorkspaceResponse;
import com.cobip.dto.lab.LabWorkspaceSaveRequest;
import com.cobip.global.common.PageResponse;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LabWorkspaceService {

    private final LabWorkspaceRepository labWorkspaceRepository;

    @Transactional
    public LabWorkspaceResponse saveWorkspace(User user, String workspaceKey, LabWorkspaceSaveRequest request) {
        LabWorkspace workspace = labWorkspaceRepository.findByUserIdAndWorkspaceKey(user.getId(), workspaceKey)
                .orElseGet(() -> LabWorkspace.create(
                        user,
                        workspaceKey,
                        request.getTitle(),
                        request.getLanguage(),
                        request.getActiveFilePath(),
                        request.getFiles()
                ));

        if (workspace.getId() != null) {
            workspace.update(
                    request.getTitle(),
                    request.getLanguage(),
                    request.getActiveFilePath(),
                    request.getFiles()
            );
        }

        return LabWorkspaceResponse.from(labWorkspaceRepository.save(workspace));
    }

    @Transactional(readOnly = true)
    public LabWorkspaceResponse getWorkspace(User user, String workspaceKey) {
        return LabWorkspaceResponse.from(findWorkspace(user, workspaceKey));
    }

    @Transactional(readOnly = true)
    public PageResponse<LabWorkspaceResponse> getWorkspaces(User user, Pageable pageable) {
        return PageResponse.from(labWorkspaceRepository
                .findByUserIdOrderByLastOpenedAtDescIdDesc(user.getId(), pageable)
                .map(LabWorkspaceResponse::from));
    }

    @Transactional
    public void deleteWorkspace(User user, String workspaceKey) {
        labWorkspaceRepository.delete(findWorkspace(user, workspaceKey));
    }

    private LabWorkspace findWorkspace(User user, String workspaceKey) {
        return labWorkspaceRepository.findByUserIdAndWorkspaceKey(user.getId(), workspaceKey)
                .orElseThrow(() -> new CustomException(ErrorCode.LAB_WORKSPACE_NOT_FOUND));
    }
}
