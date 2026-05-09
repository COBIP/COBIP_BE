package com.cobip.domain.report;

import com.cobip.domain.user.User;
import com.cobip.dto.report.ReportCreateRequest;
import com.cobip.dto.report.ReportResponse;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    @Transactional
    public ReportResponse createReport(User reporter, ReportCreateRequest request) {
        if (reporter == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }

        Report report = reportRepository.save(Report.builder()
                .reporter(reporter)
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .reason(request.getReason())
                .description(request.getDescription())
                .status(ReportStatus.PENDING)
                .build());

        return ReportResponse.from(report);
    }
}
