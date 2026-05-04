package com.cobip.domain.activity;

import com.cobip.domain.user.User;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityHistoryService {

    private final ActivityHistoryRepository activityHistoryRepository;

    public void record(User user, ActivityType type, String message, String targetType, Long targetId) {
        activityHistoryRepository.save(ActivityHistory.builder()
                .user(user)
                .type(type)
                .message(message)
                .targetType(targetType)
                .targetId(targetId)
                .build());
    }
}
