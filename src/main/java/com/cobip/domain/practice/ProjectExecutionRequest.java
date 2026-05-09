package com.cobip.domain.practice;

import java.util.List;

public record ProjectExecutionRequest(
    List<ProjectExecutionFile> files,
    String command,
    String dockerImage,
    int timeLimitMillis,
    int memoryLimitMb
) {
}
