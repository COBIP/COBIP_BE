package com.cobip.infra.judge0;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.cobip.domain.coding.CodeExecutionClient;
import com.cobip.domain.coding.CodeExecutionResult;
import com.cobip.domain.coding.CodingLanguage;
import com.cobip.domain.coding.CodingSubmissionStatus;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class Judge0CodeExecutionClient implements CodeExecutionClient {

    private static final int STATUS_ACCEPTED = 3;
    private static final int STATUS_WRONG_ANSWER = 4;
    private static final int STATUS_TIME_LIMIT_EXCEEDED = 5;
    private static final int STATUS_COMPILATION_ERROR = 6;

    private final RestClient restClient;
    private final String authHeader;
    private final String authToken;
    private final long pollTimeoutMillis;
    private final long pollIntervalMillis;

    public Judge0CodeExecutionClient(
        RestClient.Builder restClientBuilder,
        @Value("${app.judge0.base-url:https://ce.judge0.com}") String baseUrl,
        @Value("${app.judge0.auth-header:X-Auth-Token}") String authHeader,
        @Value("${app.judge0.auth-token:}") String authToken,
        @Value("${app.judge0.poll-timeout-millis:10000}") long pollTimeoutMillis,
        @Value("${app.judge0.poll-interval-millis:250}") long pollIntervalMillis
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.authHeader = authHeader;
        this.authToken = authToken;
        this.pollTimeoutMillis = pollTimeoutMillis;
        this.pollIntervalMillis = pollIntervalMillis;
    }

    @Override
    public CodeExecutionResult execute(
        CodingLanguage language,
        String sourceCode,
        String input,
        String expectedOutput,
        int timeLimitMillis,
        int memoryLimitMb
    ) {
        try {
            SubmissionCreateResponse created = createSubmission(new SubmissionCreateRequest(
                    encode(sourceCode),
                    languageId(language),
                    encode(input == null ? "" : input),
                    expectedOutput == null ? null : encode(expectedOutput),
                    Math.max(0.1, timeLimitMillis / 1000.0),
                    Math.max(1.0, timeLimitMillis / 1000.0 + 1.0),
                    memoryLimitMb * 1024
            ));

            if (created == null || created.token() == null || created.token().isBlank()) {
                throw new CustomException(ErrorCode.CODE_EXECUTION_FAILED);
            }

            return pollResult(created.token());
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.CODE_EXECUTION_FAILED, e);
        }
    }

    private SubmissionCreateResponse createSubmission(SubmissionCreateRequest request) {
        return restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/submissions")
                        .queryParam("base64_encoded", "true")
                        .queryParam("wait", "false")
                        .build())
                .headers(this::addAuthHeader)
                .body(request)
                .retrieve()
                .body(SubmissionCreateResponse.class);
    }

    private CodeExecutionResult pollResult(String token) {
        long deadline = System.currentTimeMillis() + pollTimeoutMillis;

        while (System.currentTimeMillis() <= deadline) {
            SubmissionResultResponse result = getSubmission(token);
            if (result == null || result.status() == null) {
                throw new CustomException(ErrorCode.CODE_EXECUTION_FAILED);
            }
            int statusId = result.status().id();
            if (statusId > 2) {
                return new CodeExecutionResult(
                        mapStatus(statusId),
                        result.token(),
                        decode(result.stdout()),
                        decode(result.stderr()),
                        decode(result.compileOutput()),
                        decode(result.message()),
                        result.time(),
                        result.memory()
                );
            }
            sleep();
        }

        throw new CustomException(ErrorCode.CODE_EXECUTION_FAILED);
    }

    private SubmissionResultResponse getSubmission(String token) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/submissions/{token}")
                        .queryParam("base64_encoded", "true")
                        .queryParam("fields", "token,stdout,stderr,compile_output,message,status,time,memory")
                        .build(token))
                .headers(this::addAuthHeader)
                .retrieve()
                .body(SubmissionResultResponse.class);
    }

    private void addAuthHeader(HttpHeaders headers) {
        if (authToken != null && !authToken.isBlank()) {
            headers.set(authHeader, authToken);
        }
    }

    private void sleep() {
        try {
            Thread.sleep(pollIntervalMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.CODE_EXECUTION_FAILED, e);
        }
    }

    private int languageId(CodingLanguage language) {
        return switch (language) {
            case JAVA -> 62;
            case JAVASCRIPT -> 63;
            case PYTHON -> 71;
        };
    }

    private CodingSubmissionStatus mapStatus(int judge0StatusId) {
        if (judge0StatusId == STATUS_ACCEPTED) {
            return CodingSubmissionStatus.ACCEPTED;
        }
        if (judge0StatusId == STATUS_WRONG_ANSWER) {
            return CodingSubmissionStatus.WRONG_ANSWER;
        }
        if (judge0StatusId == STATUS_TIME_LIMIT_EXCEEDED) {
            return CodingSubmissionStatus.TIME_LIMIT_EXCEEDED;
        }
        if (judge0StatusId == STATUS_COMPILATION_ERROR) {
            return CodingSubmissionStatus.COMPILE_ERROR;
        }
        if (judge0StatusId >= 7 && judge0StatusId <= 12) {
            return CodingSubmissionStatus.RUNTIME_ERROR;
        }
        return CodingSubmissionStatus.INTERNAL_ERROR;
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        if (value == null) {
            return null;
        }
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }

    private record SubmissionCreateRequest(
        @JsonProperty("source_code") String sourceCode,
        @JsonProperty("language_id") int languageId,
        String stdin,
        @JsonProperty("expected_output") String expectedOutput,
        @JsonProperty("cpu_time_limit") double cpuTimeLimit,
        @JsonProperty("wall_time_limit") double wallTimeLimit,
        @JsonProperty("memory_limit") int memoryLimit
    ) {
    }

    private record SubmissionCreateResponse(String token) {
    }

    private record SubmissionResultResponse(
        String token,
        String stdout,
        String stderr,
        @JsonProperty("compile_output") String compileOutput,
        String message,
        Status status,
        String time,
        Integer memory
    ) {
    }

    private record Status(int id, String description) {
    }
}
