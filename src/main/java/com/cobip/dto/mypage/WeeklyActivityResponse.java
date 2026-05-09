package com.cobip.dto.mypage;

import java.time.LocalDate;

import lombok.Getter;

@Getter
public class WeeklyActivityResponse {

    private final LocalDate date;
    private final long activityCount;

    public WeeklyActivityResponse(LocalDate date, long activityCount) {
        this.date = date;
        this.activityCount = activityCount;
    }
}
