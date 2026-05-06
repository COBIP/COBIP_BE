package com.cobip.domain.report;

import java.util.ArrayList;
import java.util.List;

import com.cobip.domain.activity.ActivityHistoryService;
import com.cobip.domain.activity.ActivityType;
import com.cobip.domain.user.User;
import com.cobip.dto.admin.AdminReportDetailResponse;
import com.cobip.dto.admin.AdminReportStatusUpdateRequest;
import com.cobip.dto.admin.AdminReportSummaryResponse;
import com.cobip.global.common.PageResponse;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final ActivityHistoryService activityHistoryService;

    @Transactional(readOnly = true)
    public PageResponse<AdminReportSummaryResponse> getReports(
        ReportStatus status,
        ReportTargetType targetType,
        Long reporterId,
        Long targetId,
        String keyword,
        Pageable pageable
    ) {
        return PageResponse.from(reportRepository.findAll(
                reportSpec(status, targetType, reporterId, targetId, keyword),
                pageable
        ).map(AdminReportSummaryResponse::from));
    }

    @Transactional(readOnly = true)
    public AdminReportDetailResponse getReport(Long reportId) {
        return AdminReportDetailResponse.from(findReport(reportId));
    }

    @Transactional
    public AdminReportDetailResponse updateStatus(
        Long reportId,
        AdminReportStatusUpdateRequest request,
        User adminUser
    ) {
        Report report = findReport(reportId);
        report.updateStatus(request.getStatus(), request.getAdminMemo(), adminUser);
        if (adminUser != null) {
            activityHistoryService.record(
                    adminUser,
                    ActivityType.REPORT_STATUS_CHANGED,
                    "Report status changed to " + request.getStatus() + ".",
                    "REPORT",
                    report.getId()
            );
        }
        return AdminReportDetailResponse.from(report);
    }

    private Report findReport(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));
    }

    private Specification<Report> reportSpec(
        ReportStatus status,
        ReportTargetType targetType,
        Long reporterId,
        Long targetId,
        String keyword
    ) {
        return (root, query, criteriaBuilder) -> {
            if (query != null) {
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (targetType != null) {
                predicates.add(criteriaBuilder.equal(root.get("targetType"), targetType));
            }
            if (reporterId != null) {
                predicates.add(criteriaBuilder.equal(root.get("reporter").get("id"), reporterId));
            }
            if (targetId != null) {
                predicates.add(criteriaBuilder.equal(root.get("targetId"), targetId));
            }
            if (keyword != null && !keyword.isBlank()) {
                Join<Report, User> reporter = root.join("reporter", JoinType.LEFT);
                String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("reason")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(reporter.get("email")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(reporter.get("nickname")), likeKeyword)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
