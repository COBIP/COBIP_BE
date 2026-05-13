package com.cobip.infra.coding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.cobip.domain.coding.CodeExecutionClient;
import com.cobip.domain.coding.CodeExecutionResult;
import com.cobip.domain.coding.CodingLanguage;
import com.cobip.domain.coding.CodingSubmissionStatus;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

@Component
public class DockerCodeExecutionClient implements CodeExecutionClient {

    private static final int MAX_OUTPUT_BYTES = 20000;
    private static final long OUTPUT_TIMEOUT_MILLIS = 1000;
    private static final long TIMEOUT_OUTPUT_TIMEOUT_MILLIS = 100;
    private static final long CLEANUP_TIMEOUT_SECONDS = 1;

    private final String pythonImage;
    private final String javascriptImage;
    private final String javaImage;
    private final long timeoutPaddingMillis;

    public DockerCodeExecutionClient(
        @Value("${app.code-execution.docker.python-image:python:3.12-alpine}") String pythonImage,
        @Value("${app.code-execution.docker.javascript-image:node:22-alpine}") String javascriptImage,
        @Value("${app.code-execution.docker.java-image:eclipse-temurin:21-jdk-alpine}") String javaImage,
        @Value("${app.code-execution.docker.timeout-padding-millis:5000}") long timeoutPaddingMillis
    ) {
        this.pythonImage = pythonImage;
        this.javascriptImage = javascriptImage;
        this.javaImage = javaImage;
        this.timeoutPaddingMillis = timeoutPaddingMillis;
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
        Path workspace = createWorkspace();
        String containerName = "cobip-code-" + UUID.randomUUID();
        long startedAt = System.currentTimeMillis();
        try {
            writeSource(workspace, language, sourceCode);
            Process process = new ProcessBuilder(dockerCommand(workspace, language, memoryLimitMb, containerName))
                    .start();
            CompletableFuture<String> stdout = CompletableFuture.supplyAsync(() -> drain(process.getInputStream()));
            CompletableFuture<String> stderr = CompletableFuture.supplyAsync(() -> drain(process.getErrorStream()));
            writeInput(process, input);

            boolean finished = process.waitFor(timeoutMillis(timeLimitMillis), TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                return new CodeExecutionResult(
                        CodingSubmissionStatus.TIME_LIMIT_EXCEEDED,
                        containerName,
                        outputOf(stdout, TIMEOUT_OUTPUT_TIMEOUT_MILLIS),
                        outputOf(stderr, TIMEOUT_OUTPUT_TIMEOUT_MILLIS),
                        null,
                        "Code execution timed out.",
                        elapsed(startedAt),
                        null
                );
            }

            String stdoutText = outputOf(stdout, OUTPUT_TIMEOUT_MILLIS);
            String stderrText = outputOf(stderr, OUTPUT_TIMEOUT_MILLIS);
            int exitCode = process.exitValue();
            CodingSubmissionStatus status = statusFor(language, exitCode, stdoutText, stderrText, expectedOutput);
            return new CodeExecutionResult(
                    status,
                    containerName,
                    stdoutText,
                    runtimeStderrFor(status, stderrText),
                    compileOutputFor(language, status, stderrText),
                    messageFor(status),
                    elapsed(startedAt),
                    null
            );
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return internalError(startedAt);
        } catch (RuntimeException e) {
            return internalError(startedAt);
        } finally {
            cleanupContainer(containerName);
            FileSystemUtils.deleteRecursively(workspace.toFile());
        }
    }

    private Path createWorkspace() {
        try {
            return Files.createTempDirectory("cobip-code-");
        } catch (IOException e) {
            throw new CustomException(ErrorCode.CODE_EXECUTION_FAILED, e);
        }
    }

    private void writeSource(Path workspace, CodingLanguage language, String sourceCode) throws IOException {
        Files.writeString(workspace.resolve(sourceFilename(language)), sourceCode == null ? "" : sourceCode, StandardCharsets.UTF_8);
    }

    List<String> dockerCommand(Path workspace, CodingLanguage language, int memoryLimitMb, String containerName) {
        int memoryLimit = Math.max(1, memoryLimitMb);
        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("run");
        command.add("--rm");
        command.add("--name");
        command.add(containerName);
        command.add("--user");
        command.add("0:0");
        command.add("--network");
        command.add("none");
        command.add("--memory");
        command.add(memoryLimit + "m");
        command.add("--memory-swap");
        command.add(memoryLimit + "m");
        command.add("--cpus");
        command.add("1");
        command.add("--pids-limit");
        command.add("128");
        command.add("--security-opt");
        command.add("no-new-privileges");
        command.add("-i");
        command.add("-v");
        command.add(workspace.toAbsolutePath() + ":/workspace");
        command.add("-w");
        command.add("/workspace");
        command.add(imageFor(language));
        command.add("sh");
        command.add("-lc");
        command.add(executionCommand(language));
        return command;
    }

    String executionCommand(CodingLanguage language) {
        return switch (language) {
            case PYTHON -> "python /workspace/main.py";
            case JAVASCRIPT -> "node /workspace/main.js";
            case JAVA -> "javac /workspace/Main.java && java -cp /workspace Main";
        };
    }

    CodingSubmissionStatus statusFor(
        CodingLanguage language,
        int exitCode,
        String stdout,
        String stderr,
        String expectedOutput
    ) {
        if (exitCode == 0) {
            if (expectedOutput == null || normalizeOutput(stdout).equals(normalizeOutput(expectedOutput))) {
                return CodingSubmissionStatus.ACCEPTED;
            }
            return CodingSubmissionStatus.WRONG_ANSWER;
        }
        if (exitCode >= 125) {
            return CodingSubmissionStatus.INTERNAL_ERROR;
        }
        if (isCompileError(language, stderr)) {
            return CodingSubmissionStatus.COMPILE_ERROR;
        }
        return CodingSubmissionStatus.RUNTIME_ERROR;
    }

    private String sourceFilename(CodingLanguage language) {
        return switch (language) {
            case PYTHON -> "main.py";
            case JAVASCRIPT -> "main.js";
            case JAVA -> "Main.java";
        };
    }

    private String imageFor(CodingLanguage language) {
        return switch (language) {
            case PYTHON -> pythonImage;
            case JAVASCRIPT -> javascriptImage;
            case JAVA -> javaImage;
        };
    }

    private boolean isCompileError(CodingLanguage language, String stderr) {
        if (stderr == null || stderr.isBlank()) {
            return false;
        }
        return switch (language) {
            case JAVA -> stderr.contains("error:") || stderr.contains("Main.java");
            case PYTHON -> stderr.contains("SyntaxError") || stderr.contains("IndentationError");
            case JAVASCRIPT -> stderr.contains("SyntaxError");
        };
    }

    private String compileOutputFor(CodingLanguage language, CodingSubmissionStatus status, String stderr) {
        return status == CodingSubmissionStatus.COMPILE_ERROR ? stderr : null;
    }

    private String runtimeStderrFor(CodingSubmissionStatus status, String stderr) {
        return status == CodingSubmissionStatus.RUNTIME_ERROR || status == CodingSubmissionStatus.INTERNAL_ERROR
                ? stderr
                : null;
    }

    private String messageFor(CodingSubmissionStatus status) {
        return switch (status) {
            case ACCEPTED, WRONG_ANSWER -> null;
            case TIME_LIMIT_EXCEEDED -> "Code execution timed out.";
            case COMPILE_ERROR -> "Code compilation failed.";
            case RUNTIME_ERROR -> "Code execution failed at runtime.";
            default -> "Code execution container failed.";
        };
    }

    private void writeInput(Process process, String input) {
        try (OutputStream stdin = process.getOutputStream()) {
            stdin.write((input == null ? "" : input).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            // The process may exit before reading stdin, for example on compile errors.
        }
    }

    private void cleanupContainer(String containerName) {
        try {
            Process cleanup = new ProcessBuilder("docker", "rm", "-f", containerName)
                    .redirectErrorStream(true)
                    .start();
            if (!cleanup.waitFor(CLEANUP_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                cleanup.destroyForcibly();
            }
        } catch (IOException e) {
            // Best-effort cleanup only.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private CodeExecutionResult internalError(long startedAt) {
        return new CodeExecutionResult(
                CodingSubmissionStatus.INTERNAL_ERROR,
                null,
                null,
                null,
                null,
                "Code execution container failed.",
                elapsed(startedAt),
                null
        );
    }

    private String outputOf(CompletableFuture<String> output, long timeoutMillis) {
        try {
            return output.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        } catch (ExecutionException | TimeoutException | CompletionException e) {
            return "";
        }
    }

    private String drain(InputStream inputStream) {
        try (inputStream; ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            int retained = 0;
            while ((read = inputStream.read(buffer)) != -1) {
                int retainable = Math.min(read, MAX_OUTPUT_BYTES - retained);
                if (retainable > 0) {
                    outputStream.write(buffer, 0, retainable);
                    retained += retainable;
                }
            }
            return outputStream.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.CODE_EXECUTION_FAILED, e);
        }
    }

    private long timeoutMillis(int timeLimitMillis) {
        return Math.max(1000L, timeLimitMillis) + Math.max(0L, timeoutPaddingMillis);
    }

    private String normalizeOutput(String output) {
        return output == null ? "" : output.stripTrailing();
    }

    private String elapsed(long startedAt) {
        double seconds = Duration.ofMillis(System.currentTimeMillis() - startedAt).toMillis() / 1000.0;
        return String.format(Locale.ROOT, "%.3f", seconds);
    }
}
