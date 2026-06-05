package com.cobip.domain.learning;

import java.time.LocalDateTime;

import com.cobip.domain.common.BaseTimeEntity;
import com.cobip.domain.user.User;
import com.fasterxml.jackson.databind.JsonNode;

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

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
    name = "ai_template_progresses",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_ai_template_progress_user_template",
        columnNames = {"user_id", "ai_template_id"}
    )
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiTemplateProgress extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ai_template_id", nullable = false, length = 120)
    private String aiTemplateId;

    @Column(name = "template_title", nullable = false, length = 120)
    private String templateTitle;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "template_snapshot_json", nullable = false, columnDefinition = "jsonb")
    private JsonNode templateSnapshotJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sections_json", columnDefinition = "jsonb")
    private JsonNode sectionsJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "last_learning_position_json", columnDefinition = "jsonb")
    private JsonNode lastLearningPositionJson;

    @Column(nullable = false)
    private int progressPercent;

    @Column(length = 255)
    private String lastStep;

    @Column(nullable = false)
    private long studySeconds;

    @Column(nullable = false)
    private boolean completed;

    private LocalDateTime lastAccessedAt;

    public static AiTemplateProgress create(
        User user,
        String aiTemplateId,
        String templateTitle,
        JsonNode templateSnapshotJson,
        JsonNode sectionsJson,
        JsonNode lastLearningPositionJson,
        int progressPercent,
        String lastStep,
        long studySeconds,
        Boolean completed,
        LocalDateTime lastAccessedAt
    ) {
        return AiTemplateProgress.builder()
                .user(user)
                .aiTemplateId(aiTemplateId)
                .templateTitle(templateTitle)
                .templateSnapshotJson(templateSnapshotJson)
                .sectionsJson(sectionsJson)
                .lastLearningPositionJson(lastLearningPositionJson)
                .progressPercent(progressPercent)
                .lastStep(lastStep)
                .studySeconds(studySeconds)
                .completed(Boolean.TRUE.equals(completed) || progressPercent >= 100)
                .lastAccessedAt(lastAccessedAt == null ? LocalDateTime.now() : lastAccessedAt)
                .build();
    }

    public void overwrite(
        String templateTitle,
        JsonNode templateSnapshotJson,
        JsonNode sectionsJson,
        JsonNode lastLearningPositionJson,
        int progressPercent,
        String lastStep,
        long studySeconds,
        Boolean completed,
        LocalDateTime lastAccessedAt
    ) {
        this.templateTitle = templateTitle;
        this.templateSnapshotJson = templateSnapshotJson;
        this.sectionsJson = sectionsJson;
        this.lastLearningPositionJson = lastLearningPositionJson;
        this.progressPercent = progressPercent;
        this.lastStep = lastStep;
        this.studySeconds = studySeconds;
        this.completed = Boolean.TRUE.equals(completed) || progressPercent >= 100;
        this.lastAccessedAt = lastAccessedAt == null ? LocalDateTime.now() : lastAccessedAt;
    }

    public void update(
        String templateTitle,
        JsonNode templateSnapshotJson,
        JsonNode sectionsJson,
        JsonNode lastLearningPositionJson,
        Integer progressPercent,
        String lastStep,
        Long studySeconds,
        Boolean completed,
        LocalDateTime lastAccessedAt
    ) {
        if (templateTitle != null) {
            this.templateTitle = templateTitle;
        }
        if (templateSnapshotJson != null) {
            this.templateSnapshotJson = templateSnapshotJson;
        }
        if (sectionsJson != null) {
            this.sectionsJson = sectionsJson;
        }
        if (lastLearningPositionJson != null) {
            this.lastLearningPositionJson = lastLearningPositionJson;
        }
        if (progressPercent != null) {
            this.progressPercent = progressPercent;
        }
        if (lastStep != null) {
            this.lastStep = lastStep;
        }
        if (studySeconds != null) {
            this.studySeconds = studySeconds;
        }
        this.completed = completed != null ? completed : this.progressPercent >= 100;
        this.lastAccessedAt = lastAccessedAt == null ? LocalDateTime.now() : lastAccessedAt;
    }
}
