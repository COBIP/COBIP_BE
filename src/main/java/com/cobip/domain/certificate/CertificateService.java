package com.cobip.domain.certificate;

import com.cobip.domain.activity.ActivityHistoryService;
import com.cobip.domain.activity.ActivityType;
import com.cobip.domain.learning.LearningProgress;
import com.cobip.domain.learning.LearningProgressRepository;
import com.cobip.domain.template.Template;
import com.cobip.domain.template.TemplateService;
import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRepository;
import com.cobip.dto.mypage.CertificateResponse;
import com.cobip.global.common.PageResponse;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final UserRepository userRepository;
    private final TemplateService templateService;
    private final ActivityHistoryService activityHistoryService;

    @Transactional
    public CertificateResponse issueCertificate(User user, Long templateId) {
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (certificateRepository.existsByUserIdAndTemplateId(managedUser.getId(), templateId)) {
            throw new CustomException(ErrorCode.CERTIFICATE_ALREADY_ISSUED);
        }

        LearningProgress progress = learningProgressRepository.findByUserIdAndTemplateId(managedUser.getId(), templateId)
                .orElseThrow(() -> new CustomException(ErrorCode.CERTIFICATE_NOT_ALLOWED));
        if (!progress.isCompleted()) {
            throw new CustomException(ErrorCode.CERTIFICATE_NOT_ALLOWED);
        }

        Template template = templateService.getActiveTemplate(templateId);
        Certificate certificate = certificateRepository.save(Certificate.issue(managedUser, template));
        activityHistoryService.record(managedUser, ActivityType.CERTIFICATE_ISSUED, "수료증을 발급했습니다.", "CERTIFICATE", certificate.getId());
        return CertificateResponse.from(certificate);
    }

    @Transactional(readOnly = true)
    public PageResponse<CertificateResponse> getMyCertificates(User user, Pageable pageable) {
        Page<CertificateResponse> certificates = certificateRepository.findByUserIdOrderByIssuedAtDesc(user.getId(), pageable)
                .map(CertificateResponse::from);
        return PageResponse.from(certificates);
    }
}
