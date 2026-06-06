package com.cobip.domain.learning;

import java.time.LocalDateTime;

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
    name = "learning_progresses",
    uniqueConstraints = @UniqueConstraint(name = "uk_learning_progress_user_template", columnNames = {"user_id", "template_id"})
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LearningProgress extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @Column(nullable = false)
    private int progressPercent;

    @Column(length = 255)
    private String lastStep;

    @Column(nullable = false)
    private int solvedCount;

    @Column(nullable = false)
    private int correctCount;

    @Column(nullable = false)
    private long studySeconds;

    private LocalDateTime lastAccessedAt;

    public boolean isCompleted() {
        return progressPercent >= 100;
    }

    public static LearningProgress start(User user, Template template) {
        return LearningProgress.builder()
                .user(user)
                .template(template)
                .progressPercent(0)
                .solvedCount(0)
                .correctCount(0)
                .studySeconds(0)
                .lastAccessedAt(LocalDateTime.now())
                .build();
    }

    public void recordQuizSubmission(int progressPercent, String lastStep, boolean correct) {
        this.progressPercent = Math.max(this.progressPercent, progressPercent);
        this.lastStep = lastStep;
        this.solvedCount++;
        if (correct) {
            this.correctCount++;
        }
        this.lastAccessedAt = LocalDateTime.now();
    }
}
