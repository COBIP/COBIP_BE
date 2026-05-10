package com.cobip.domain.coding;

import java.time.LocalDateTime;

import com.cobip.domain.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "coding_workbooks")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CodingWorkbook extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 80)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CodingDifficulty difficulty;

    @Column(nullable = false, length = 500)
    private String summary;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CodingWorkbookStatus status;

    @Column(nullable = false)
    private int displayOrder;

    private LocalDateTime deletedAt;

    public void update(
        String slug,
        String title,
        String category,
        CodingDifficulty difficulty,
        String summary,
        String description,
        CodingWorkbookStatus status,
        Integer displayOrder
    ) {
        if (slug != null) {
            this.slug = slug;
        }
        if (title != null) {
            this.title = title;
        }
        if (category != null) {
            this.category = category;
        }
        if (difficulty != null) {
            this.difficulty = difficulty;
        }
        if (summary != null) {
            this.summary = summary;
        }
        if (description != null) {
            this.description = description;
        }
        if (status != null) {
            this.status = status;
        }
        if (displayOrder != null) {
            this.displayOrder = displayOrder;
        }
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
