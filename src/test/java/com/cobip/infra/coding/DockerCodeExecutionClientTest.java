package com.cobip.infra.coding;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import com.cobip.domain.coding.CodingLanguage;
import com.cobip.domain.coding.CodingSubmissionStatus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DockerCodeExecutionClientTest {

    private final DockerCodeExecutionClient client = new DockerCodeExecutionClient(
            "python:3.12-alpine",
            "node:22-alpine",
            "eclipse-temurin:21-jdk-alpine",
            5000
    );

    @TempDir
    Path workspace;

    @Test
    void dockerCommandRunsPythonInSandboxedContainer() {
        var command = client.dockerCommand(workspace, CodingLanguage.PYTHON, 128, "cobip-code-test");

        assertThat(command).containsSubsequence("docker", "run", "--rm");
        assertThat(command).contains("--network", "none", "--memory", "128m", "--pids-limit", "128");
        assertThat(command).containsSubsequence("python:3.12-alpine", "sh", "-c", "python /workspace/main.py");
        assertThat(command).contains("-i", "python:3.12-alpine", "python /workspace/main.py");
    }

    @Test
    void executionCommandCompilesAndRunsJavaMainClass() {
        assertThat(client.executionCommand(CodingLanguage.JAVA))
                .isEqualTo("javac /workspace/Main.java && java -cp /workspace Main");
    }

    @Test
    void statusForAcceptedWhenExpectedOutputMatchesIgnoringTrailingWhitespace() {
        var status = client.statusFor(CodingLanguage.PYTHON, 0, "3\n", "", "3");

        assertThat(status).isEqualTo(CodingSubmissionStatus.ACCEPTED);
    }

    @Test
    void statusForWrongAnswerWhenExpectedOutputDiffers() {
        var status = client.statusFor(CodingLanguage.PYTHON, 0, "4", "", "3");

        assertThat(status).isEqualTo(CodingSubmissionStatus.WRONG_ANSWER);
    }

    @Test
    void statusForCompileErrorWhenSyntaxErrorOccurs() {
        var status = client.statusFor(CodingLanguage.PYTHON, 1, "", "SyntaxError: invalid syntax", null);

        assertThat(status).isEqualTo(CodingSubmissionStatus.COMPILE_ERROR);
    }

    @Test
    void statusForRuntimeErrorWhenProgramExitsNonZero() {
        var status = client.statusFor(CodingLanguage.JAVASCRIPT, 1, "", "ReferenceError: x is not defined", null);

        assertThat(status).isEqualTo(CodingSubmissionStatus.RUNTIME_ERROR);
    }

    @Test
    void statusForInternalErrorWhenDockerRunFails() {
        var status = client.statusFor(CodingLanguage.PYTHON, 125, "", "docker daemon error", null);

        assertThat(status).isEqualTo(CodingSubmissionStatus.INTERNAL_ERROR);
    }
}
