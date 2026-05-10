package com.cobip.domain.coding;

import java.time.LocalDateTime;

import com.cobip.domain.common.BaseTimeEntity;
import com.fasterxml.jackson.databind.JsonNode;

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

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "coding_problems")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CodingProblem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workbook_id", nullable = false)
    private CodingWorkbook workbook;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 80)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CodingDifficulty difficulty;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content_json", nullable = false, columnDefinition = "jsonb")
    private JsonNode contentJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "explanation_json", columnDefinition = "jsonb")
    private JsonNode explanationJson;

    @Column(nullable = false)
    private int orderIndex;

    @Column(nullable = false)
    private int timeLimitMillis;

    @Column(nullable = false)
    private int memoryLimitMb;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CodingProblemStatus status;

    private LocalDateTime deletedAt;

    public void update(
        String title,
        String category,
        CodingDifficulty difficulty,
        JsonNode contentJson,
        JsonNode explanationJson,
        Integer orderIndex,
        Integer timeLimitMillis,
        Integer memoryLimitMb,
        CodingProblemStatus status
    ) {
        if (title != null) {
            this.title = title;
        }
        if (category != null) {
            this.category = category;
        }
        if (difficulty != null) {
            this.difficulty = difficulty;
        }
        if (contentJson != null) {
            this.contentJson = contentJson;
        }
        if (explanationJson != null) {
            this.explanationJson = explanationJson;
        }
        if (orderIndex != null) {
            this.orderIndex = orderIndex;
        }
        if (timeLimitMillis != null) {
            this.timeLimitMillis = timeLimitMillis;
        }
        if (memoryLimitMb != null) {
            this.memoryLimitMb = memoryLimitMb;
        }
        if (status != null) {
            this.status = status;
        }
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
