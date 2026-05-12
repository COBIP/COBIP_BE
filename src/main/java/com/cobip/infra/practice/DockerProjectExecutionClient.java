package com.cobip.infra.practice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.cobip.domain.practice.ProjectExecutionClient;
import com.cobip.domain.practice.ProjectExecutionFile;
import com.cobip.domain.practice.ProjectExecutionRequest;
import com.cobip.domain.practice.ProjectExecutionResult;
import com.cobip.domain.practice.TemplatePracticeSubmissionStatus;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

@Component
public class DockerProjectExecutionClient implements ProjectExecutionClient {

    private static final int MAX_OUTPUT_BYTES = 20000;

    @Override
    public ProjectExecutionResult execute(ProjectExecutionRequest request) {
        Path workspace = createWorkspace();
        String containerName = "cobip-practice-" + UUID.randomUUID();
        long startedAt = System.currentTimeMillis();
        try {
            writeFiles(workspace, request.files());
            Process process = new ProcessBuilder(dockerCommand(workspace, request, containerName)).start();
            CompletableFuture<String> stdout = CompletableFuture.supplyAsync(() -> drain(process.getInputStream()));
            CompletableFuture<String> stderr = CompletableFuture.supplyAsync(() -> drain(process.getErrorStream()));
            boolean finished = process.waitFor(request.timeLimitMillis(), TimeUnit.MILLISECONDS);

            if (!finished) {
                process.destroyForcibly();
                cleanupContainer(containerName);
                return new ProjectExecutionResult(
                        TemplatePracticeSubmissionStatus.TIME_LIMIT_EXCEEDED,
                        -1,
                        outputOf(stdout),
                        outputOf(stderr),
                        "Project execution timed out.",
                        elapsed(startedAt)
                );
            }

            int exitCode = process.exitValue();
            return new ProjectExecutionResult(
                    exitCode == 0
                            ? TemplatePracticeSubmissionStatus.ACCEPTED
                            : TemplatePracticeSubmissionStatus.RUNTIME_ERROR,
                    exitCode,
                    outputOf(stdout),
                    outputOf(stderr),
                    null,
                    elapsed(startedAt)
            );
        } catch (CustomException e) {
            if (e.getErrorCode() == ErrorCode.INVALID_REQUEST) {
                throw e;
            }
            return internalError(startedAt, e);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return internalError(startedAt, e);
        } catch (RuntimeException e) {
            return internalError(startedAt, e);
        } finally {
            cleanupContainer(containerName);
            FileSystemUtils.deleteRecursively(workspace.toFile());
        }
    }

    private Path createWorkspace() {
        try {
            return Files.createTempDirectory("cobip-practice-");
        } catch (IOException e) {
            throw new CustomException(ErrorCode.CODE_EXECUTION_FAILED, e);
        }
    }

    private void writeFiles(Path workspace, List<ProjectExecutionFile> files) throws IOException {
        for (ProjectExecutionFile file : files) {
            Path target = workspace.resolve(file.filePath()).normalize();
            if (!target.startsWith(workspace) || Files.isDirectory(target)) {
                throw new CustomException(ErrorCode.INVALID_REQUEST);
            }
            Files.createDirectories(target.getParent());
            Files.writeString(target, file.content(), StandardCharsets.UTF_8);
        }
    }

    private List<String> dockerCommand(Path workspace, ProjectExecutionRequest request, String containerName) {
        int memoryLimitMb = Math.max(1, request.memoryLimitMb());
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
        command.add(memoryLimitMb + "m");
        command.add("--memory-swap");
        command.add(memoryLimitMb + "m");
        command.add("--cpus");
        command.add("1");
        command.add("--pids-limit");
        command.add("128");
        command.add("--security-opt");
        command.add("no-new-privileges");
        command.add("-v");
        command.add(workspace.toAbsolutePath() + ":/workspace");
        command.add("-w");
        command.add("/workspace");
        command.add(request.dockerImage());
        command.add("sh");
        command.add("-lc");
        command.add(request.command());
        return command;
    }

    private void cleanupContainer(String containerName) {
        try {
            Process cleanup = new ProcessBuilder("docker", "rm", "-f", containerName)
                    .redirectErrorStream(true)
                    .start();
            if (!cleanup.waitFor(5, TimeUnit.SECONDS)) {
                cleanup.destroyForcibly();
            }
        } catch (IOException e) {
            // Best-effort cleanup only.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private ProjectExecutionResult internalError(long startedAt, RuntimeException e) {
        return internalError(startedAt, (Exception)e);
    }

    private ProjectExecutionResult internalError(long startedAt, Exception e) {
        return new ProjectExecutionResult(
                TemplatePracticeSubmissionStatus.INTERNAL_ERROR,
                -1,
                "",
                "",
                "Project execution failed.",
                elapsed(startedAt)
        );
    }

    private String outputOf(CompletableFuture<String> output) {
        try {
            return output.get(1, TimeUnit.SECONDS);
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

    private long elapsed(long startedAt) {
        return Duration.ofMillis(System.currentTimeMillis() - startedAt).toMillis();
    }
}
