package com.cobip.domain.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRole;
import com.cobip.domain.user.UserStatus;
import com.cobip.dto.report.ReportCreateRequest;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(reportRepository);
    }

    @Test
    void createReportSavesPendingReport() {
        User reporter = user(1L);
        ReportCreateRequest request = request();
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = reportService.createReport(reporter, request);

        assertThat(response.getReporterId()).isEqualTo(1L);
        assertThat(response.getTargetType()).isEqualTo(ReportTargetType.GRAMMAR_TEMPLATE);
        assertThat(response.getTargetId()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo(ReportStatus.PENDING);
    }

    @Test
    void createReportRejectsAnonymousReporter() {
        assertThatThrownBy(() -> reportService.createReport(null, request()))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.LOGIN_REQUIRED);
    }

    private ReportCreateRequest request() {
        ReportCreateRequest request = new ReportCreateRequest();
        ReflectionTestUtils.setField(request, "targetType", ReportTargetType.GRAMMAR_TEMPLATE);
        ReflectionTestUtils.setField(request, "targetId", 10L);
        ReflectionTestUtils.setField(request, "reason", "Inappropriate content");
        ReflectionTestUtils.setField(request, "description", "Contains wrong or harmful content.");
        return request;
    }

    private User user(Long id) {
        return User.builder()
                .id(id)
                .email("reporter@example.com")
                .password("encoded-password")
                .nickname("reporter")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
    }
}
