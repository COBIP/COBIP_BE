package com.cobip.domain.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.cobip.domain.activity.ActivityHistoryService;
import com.cobip.domain.activity.ActivityType;
import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRole;
import com.cobip.domain.user.UserStatus;
import com.cobip.dto.admin.AdminReportStatusUpdateRequest;
import com.cobip.global.common.PageResponse;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ActivityHistoryService activityHistoryService;

    private AdminReportService adminReportService;

    @BeforeEach
    void setUp() {
        adminReportService = new AdminReportService(reportRepository, activityHistoryService);
    }

    @Test
    void getReportsReturnsPagedReportSummaries() {
        Report report = report();
        when(reportRepository.findAll(
                anyReportSpecification(),
                any(Pageable.class)
        ))
                .thenReturn(new PageImpl<>(List.of(report)));

        PageResponse<?> response = adminReportService.getReports(
                ReportStatus.PENDING,
                ReportTargetType.TEMPLATE,
                2L,
                10L,
                "spam",
                PageRequest.of(0, 20)
        );

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    void updateStatusChangesReportStatusAndRecordsActivity() {
        Report report = report();
        User admin = user(1L, "admin@example.com", "admin", UserRole.ADMIN);
        AdminReportStatusUpdateRequest request = statusRequest(ReportStatus.RESOLVED, "Handled");
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        adminReportService.updateStatus(1L, request, admin);

        assertThat(report.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(report.getAdminMemo()).isEqualTo("Handled");
        assertThat(report.getProcessedBy()).isEqualTo(admin);
        assertThat(report.getProcessedAt()).isNotNull();
        verify(activityHistoryService).record(
                admin,
                ActivityType.REPORT_STATUS_CHANGED,
                "Report status changed to RESOLVED.",
                "REPORT",
                1L
        );
    }

    @Test
    void updateStatusRejectsMissingReport() {
        AdminReportStatusUpdateRequest request = statusRequest(ReportStatus.REJECTED, "No issue");
        when(reportRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminReportService.updateStatus(1L, request, user(
                1L,
                "admin@example.com",
                "admin",
                UserRole.ADMIN
        )))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REPORT_NOT_FOUND);
    }

    private AdminReportStatusUpdateRequest statusRequest(ReportStatus status, String adminMemo) {
        AdminReportStatusUpdateRequest request = new AdminReportStatusUpdateRequest();
        ReflectionTestUtils.setField(request, "status", status);
        ReflectionTestUtils.setField(request, "adminMemo", adminMemo);
        return request;
    }

    private Specification<Report> anyReportSpecification() {
        return any();
    }

    private Report report() {
        return Report.builder()
                .id(1L)
                .reporter(user(2L, "reporter@example.com", "reporter", UserRole.USER))
                .targetType(ReportTargetType.TEMPLATE)
                .targetId(10L)
                .reason("Inappropriate content")
                .description("Contains spam.")
                .status(ReportStatus.PENDING)
                .build();
    }

    private User user(Long id, String email, String nickname, UserRole role) {
        return User.builder()
                .id(id)
                .email(email)
                .password("encoded-password")
                .nickname(nickname)
                .role(role)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
    }
}
