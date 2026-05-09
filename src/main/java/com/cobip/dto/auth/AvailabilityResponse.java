package com.cobip.dto.auth;

import lombok.Getter;

@Getter
public class AvailabilityResponse {

    private final boolean available;

    public AvailabilityResponse(boolean available) {
        this.available = available;
    }
}
