package com.cobip.domain.lab;

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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "lab_workspaces")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LabWorkspace extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String workspaceKey;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 40)
    private String language;

    @Column(nullable = false, length = 255)
    private String activeFilePath;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "files_json", nullable = false, columnDefinition = "jsonb")
    private JsonNode filesJson;

    @Column(nullable = false)
    private LocalDateTime lastOpenedAt;

    public static LabWorkspace create(
        User user,
        String workspaceKey,
        String title,
        String language,
        String activeFilePath,
        JsonNode filesJson
    ) {
        return LabWorkspace.builder()
                .user(user)
                .workspaceKey(workspaceKey)
                .title(title)
                .language(language)
                .activeFilePath(activeFilePath)
                .filesJson(filesJson)
                .lastOpenedAt(LocalDateTime.now())
                .build();
    }

    public void update(String title, String language, String activeFilePath, JsonNode filesJson) {
        this.title = title;
        this.language = language;
        this.activeFilePath = activeFilePath;
        this.filesJson = filesJson;
        this.lastOpenedAt = LocalDateTime.now();
    }
}
