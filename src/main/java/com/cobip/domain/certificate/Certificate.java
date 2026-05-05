package com.cobip.domain.certificate;

import java.time.LocalDateTime;
import java.util.UUID;

import com.cobip.domain.common.BaseTimeEntity;
import com.cobip.domain.template.Template;
import com.cobip.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "certificates",
    uniqueConstraints = @UniqueConstraint(name = "uk_certificate_user_template", columnNames = {"user_id", "template_id"})
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Certificate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @Column(nullable = false, unique = true, length = 80)
    private String certificateNumber;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    public static Certificate issue(User user, Template template) {
        return Certificate.builder()
                .user(user)
                .template(template)
                .certificateNumber("COBIP-" + UUID.randomUUID())
                .issuedAt(LocalDateTime.now())
                .build();
    }
}
