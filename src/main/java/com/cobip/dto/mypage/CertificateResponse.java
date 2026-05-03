package com.cobip.dto.mypage;

import java.time.LocalDateTime;

import com.cobip.domain.certificate.Certificate;

import lombok.Getter;

@Getter
public class CertificateResponse {

    private final Long id;
    private final Long templateId;
    private final String templateTitle;
    private final String certificateNumber;
    private final LocalDateTime issuedAt;

    private CertificateResponse(Certificate certificate) {
        this.id = certificate.getId();
        this.templateId = certificate.getTemplate().getId();
        this.templateTitle = certificate.getTemplate().getTitle();
        this.certificateNumber = certificate.getCertificateNumber();
        this.issuedAt = certificate.getIssuedAt();
    }

    public static CertificateResponse from(Certificate certificate) {
        return new CertificateResponse(certificate);
    }
}
