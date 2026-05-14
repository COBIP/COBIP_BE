package com.cobip.domain.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.cobip.domain.grammar.GrammarTemplate;
import com.cobip.domain.grammar.GrammarTemplateChapter;
import com.cobip.domain.grammar.GrammarTemplateChapterRepository;
import com.cobip.domain.grammar.GrammarTemplateDifficulty;
import com.cobip.domain.grammar.GrammarTemplateLanguage;
import com.cobip.domain.grammar.GrammarTemplateRepository;
import com.cobip.domain.grammar.GrammarTemplateStatus;
import com.cobip.domain.user.User;
import com.cobip.domain.user.UserRole;
import com.cobip.domain.user.UserStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GrammarLearningProgressServiceTest {

    @Mock
    private GrammarTemplateRepository grammarTemplateRepository;

    @Mock
    private GrammarTemplateChapterRepository grammarTemplateChapterRepository;

    @Mock
    private GrammarLearningProgressRepository grammarLearningProgressRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private GrammarLearningProgressService grammarLearningProgressService;

    @BeforeEach
    void setUp() {
        grammarLearningProgressService = new GrammarLearningProgressService(
                grammarTemplateRepository,
                grammarTemplateChapterRepository,
                grammarLearningProgressRepository
        );
    }

    @Test
    void recordAccessCreatesProgressByCurrentChapter() {
        User user = user();
        GrammarTemplate template = template();
        GrammarTemplateChapter firstChapter = chapter(template, 10L, "Variables", 0);
        GrammarTemplateChapter secondChapter = chapter(template, 11L, "Loop", 1);
        when(grammarTemplateRepository.findByIdAndStatusAndDeletedAtIsNull(
                1L,
                GrammarTemplateStatus.PUBLISHED
        )).thenReturn(Optional.of(template));
        when(grammarTemplateChapterRepository.findByTemplateIdAndDeletedAtIsNullOrderByOrderIndexAscIdAsc(1L))
                .thenReturn(List.of(firstChapter, secondChapter));
        when(grammarLearningProgressRepository.findByUserIdAndTemplateId(1L, 1L)).thenReturn(Optional.empty());

        grammarLearningProgressService.recordAccess(user, 1L, 10L, 30);

        ArgumentCaptor<GrammarLearningProgress> progressCaptor = ArgumentCaptor
                .forClass(GrammarLearningProgress.class);
        verify(grammarLearningProgressRepository).save(progressCaptor.capture());
        GrammarLearningProgress progress = progressCaptor.getValue();
        assertThat(progress.getTemplate()).isEqualTo(template);
        assertThat(progress.getCurrentChapter()).isEqualTo(firstChapter);
        assertThat(progress.getProgressPercent()).isEqualTo(50);
        assertThat(progress.getLastStep()).isEqualTo("Variables");
        assertThat(progress.getStudySeconds()).isEqualTo(30);
        verify(grammarLearningProgressRepository).findByUserIdAndTemplateId(eq(1L), eq(1L));
    }

    private User user() {
        return User.builder()
                .id(1L)
                .email("user@example.com")
                .password("encoded-password")
                .nickname("user")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
    }

    private GrammarTemplate template() {
        return GrammarTemplate.builder()
                .id(1L)
                .slug("java-variable")
                .title("Java Variable")
                .language(GrammarTemplateLanguage.JAVA)
                .category("basic-syntax")
                .difficulty(GrammarTemplateDifficulty.BEGINNER)
                .summary("Java variable basics")
                .contentJson(objectMapper.createObjectNode().put("type", "doc"))
                .searchableText("java variable")
                .status(GrammarTemplateStatus.PUBLISHED)
                .build();
    }

    private GrammarTemplateChapter chapter(
        GrammarTemplate template,
        Long id,
        String title,
        Integer orderIndex
    ) {
        return GrammarTemplateChapter.builder()
                .id(id)
                .template(template)
                .title(title)
                .orderIndex(orderIndex)
                .contentJson(objectMapper.createObjectNode().put("type", "doc"))
                .searchableText(title)
                .build();
    }
}
