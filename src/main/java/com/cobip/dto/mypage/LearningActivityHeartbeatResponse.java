package com.cobip.dto.mypage;

import java.time.LocalDate;

import lombok.Getter;

@Getter
public class LearningActivityHeartbeatResponse {

    private final LocalDate date;
    private final long studySeconds;

    public LearningActivityHeartbeatResponse(LocalDate date, long studySeconds) {
        this.date = date;
        this.studySeconds = studySeconds;
    }
}
