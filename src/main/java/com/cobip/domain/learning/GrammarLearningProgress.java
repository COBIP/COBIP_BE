package com.cobip.domain.learning;

import java.time.LocalDateTime;

import com.cobip.domain.common.BaseTimeEntity;
import com.cobip.domain.grammar.GrammarTemplate;
import com.cobip.domain.grammar.GrammarTemplateChapter;
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
    name = "grammar_learning_progresses",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_grammar_learning_progress_user_template",
        columnNames = {"user_id", "grammar_template_id"}
    )
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GrammarLearningProgress extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grammar_template_id", nullable = false)
    private GrammarTemplate template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_chapter_id")
    private GrammarTemplateChapter currentChapter;

    @Column(nullable = false)
    private int progressPercent;

    @Column(length = 255)
    private String lastStep;

    @Column(nullable = false)
    private long studySeconds;

    private LocalDateTime lastAccessedAt;

    public static GrammarLearningProgress start(User user, GrammarTemplate template) {
        return GrammarLearningProgress.builder()
                .user(user)
                .template(template)
                .progressPercent(0)
                .studySeconds(0)
                .lastAccessedAt(LocalDateTime.now())
                .build();
    }

    public void recordAccess(GrammarTemplateChapter chapter, int nextProgressPercent, long activeSeconds) {
        this.currentChapter = chapter;
        this.progressPercent = Math.max(this.progressPercent, nextProgressPercent);
        this.lastStep = chapter == null ? null : chapter.getTitle();
        this.studySeconds += Math.max(0, activeSeconds);
        this.lastAccessedAt = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return progressPercent >= 100;
    }
}
