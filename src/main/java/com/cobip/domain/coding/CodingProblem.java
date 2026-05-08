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
@Table(name = "coding_problems")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CodingProblem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 80)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CodingProblemDifficulty difficulty;

    @Lob
    @Column(nullable = false)
    private String description;

    @Lob
    @Column(nullable = false)
    private String inputDescription;

    @Lob
    @Column(nullable = false)
    private String outputDescription;

    @Lob
    @Column(nullable = false)
    private String sampleInput;

    @Lob
    @Column(nullable = false)
    private String sampleOutput;

    @Column(nullable = false)
    private int timeLimitMillis;

    @Column(nullable = false)
    private int memoryLimitMb;

    @Column(nullable = false)
    private boolean published;

    private LocalDateTime deletedAt;
}
