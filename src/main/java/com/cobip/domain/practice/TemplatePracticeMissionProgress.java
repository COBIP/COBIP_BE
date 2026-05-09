package com.cobip.domain.practice;

import java.time.LocalDateTime;

import com.cobip.domain.common.BaseTimeEntity;
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
@Table(name = "template_practice_mission_progresses")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TemplatePracticeMissionProgress extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_id", nullable = false)
    private TemplatePracticeMission mission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TemplatePracticeMissionProgressStatus status;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    public static TemplatePracticeMissionProgress start(User user, TemplatePracticeMission mission) {
        return TemplatePracticeMissionProgress.builder()
                .user(user)
                .mission(mission)
                .status(TemplatePracticeMissionProgressStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .build();
    }

    public void changeStatus(TemplatePracticeMissionProgressStatus status) {
        this.status = status;
        if (status == TemplatePracticeMissionProgressStatus.COMPLETED) {
            this.completedAt = LocalDateTime.now();
            return;
        }
        this.completedAt = null;
    }
}
