package com.cobip.domain.practice;

import java.time.LocalDateTime;

import com.cobip.domain.common.BaseTimeEntity;
import com.cobip.domain.template.Template;
import com.cobip.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "template_practice_progresses")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TemplatePracticeProgress extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_mission_id")
    private TemplatePracticeMission currentMission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TemplatePracticeProgressStatus status;

    @Column(nullable = false)
    private int progressPercent;

    @Column(nullable = false)
    private int completedMissionCount;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @Column(nullable = false)
    private LocalDateTime lastAccessedAt;

    public static TemplatePracticeProgress start(User user, Template template, TemplatePracticeMission firstMission) {
        LocalDateTime now = LocalDateTime.now();
        return TemplatePracticeProgress.builder()
                .user(user)
                .template(template)
                .currentMission(firstMission)
                .status(TemplatePracticeProgressStatus.IN_PROGRESS)
                .progressPercent(0)
                .completedMissionCount(0)
                .startedAt(now)
                .lastAccessedAt(now)
                .build();
    }

    public void touch() {
        this.lastAccessedAt = LocalDateTime.now();
        if (status == TemplatePracticeProgressStatus.NOT_STARTED) {
            this.status = TemplatePracticeProgressStatus.IN_PROGRESS;
        }
    }

    public void updateProgress(int completedMissionCount, int totalMissionCount, TemplatePracticeMission currentMission) {
        this.completedMissionCount = completedMissionCount;
        this.progressPercent = totalMissionCount == 0 ? 0 : completedMissionCount * 100 / totalMissionCount;
        this.currentMission = currentMission;
        this.lastAccessedAt = LocalDateTime.now();
        if (totalMissionCount > 0 && completedMissionCount >= totalMissionCount) {
            this.status = TemplatePracticeProgressStatus.COMPLETED;
            this.completedAt = this.lastAccessedAt;
        } else {
            this.status = TemplatePracticeProgressStatus.IN_PROGRESS;
            this.completedAt = null;
        }
    }
}
