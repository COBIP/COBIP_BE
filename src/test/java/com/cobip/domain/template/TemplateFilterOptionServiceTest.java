package com.cobip.domain.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import com.cobip.domain.activity.ActivityHistoryService;
import com.cobip.domain.subscription.SubscriptionService;
import com.cobip.domain.user.UserRepository;
import com.cobip.infra.aws.S3Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TemplateFilterOptionServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private TemplateFavoriteRepository templateFavoriteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private ActivityHistoryService activityHistoryService;

    @Mock
    private S3Service s3Service;

    private TemplateService templateService;

    @BeforeEach
    void setUp() {
        templateService = new TemplateService(
                templateRepository,
                templateFavoriteRepository,
                userRepository,
                subscriptionService,
                activityHistoryService,
                s3Service
        );
    }

    @Test
    void getCategoriesReturnsPublicCategories() {
        when(templateRepository.findPublicCategories()).thenReturn(List.of("backend", "frontend"));

        assertThat(templateService.getCategories()).containsExactly("backend", "frontend");
    }

    @Test
    void getTechStacksReturnsPublicTechStacks() {
        when(templateRepository.findPublicTechStacks()).thenReturn(List.of("Java", "Spring"));

        assertThat(templateService.getTechStacks()).containsExactly("Java", "Spring");
    }
}
