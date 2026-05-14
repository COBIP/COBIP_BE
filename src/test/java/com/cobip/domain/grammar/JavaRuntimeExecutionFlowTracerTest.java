package com.cobip.domain.grammar;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import com.cobip.dto.grammar.GrammarTemplateExecutionFlowResponse.ExecutionFlowStep;

import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

class JavaRuntimeExecutionFlowTracerTest {

    @Test
    void instrumentedJavaCodeCompilesForArrayLoop() throws Exception {
        JavaRuntimeExecutionFlowTracer tracer = new JavaRuntimeExecutionFlowTracer();
        String sourceCode = """
                import java.util.ArrayList;
                import java.util.List;

                public class Main {
                    public static void main(String[] args) {
                        int[] arr = {3, 1, 2};
                        List<Integer> list = new ArrayList<>(List.of(3, 1, 2));
                        for (int i = 0; i < arr.length; i++) {
                            arr[i] = arr[i] + 1;
                            list.set(i, list.get(i) + 1);
                        }
                        System.out.println(arr[0]);
                    }
                }
                """;
        Path workspace = Files.createTempDirectory("cobip-trace-compile-");
        try {
            Path sourceFile = workspace.resolve("Main.java");
            Files.writeString(sourceFile, tracer.instrument(sourceCode));

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            assertThat(compiler).isNotNull();
            assertThat(compiler.run(null, null, null, sourceFile.toString())).isZero();
        } finally {
            FileSystemUtils.deleteRecursively(workspace);
        }
    }

    @Test
    void buildStepsCapturesNestedPrintLoopOutput() throws Exception {
        JavaRuntimeExecutionFlowTracer tracer = new JavaRuntimeExecutionFlowTracer();
        String sourceCode = """
                public class Main {
                    public static void main(String[] args) {
                        int n = 5;

                        for (int i = 1; i <= n; i++) {
                            // 공백 출력
                            for (int j = 1; j <= n - i; j++) {
                                System.out.print(" ");
                            }

                            // 별 출력
                            for (int j = 1; j <= 2 * i - 1; j++) {
                                System.out.print("*");
                            }

                            // 줄바꿈
                            System.out.println();
                        }
                    }
                }
                """;
        String stdout = compileAndRun(tracer, sourceCode);

        List<ExecutionFlowStep> steps = tracer.buildSteps(sourceCode, stdout);

        assertThat(steps).isNotEmpty();
        assertThat(steps)
                .filteredOn(step -> "OUTPUT".equals(step.getEventType()))
                .hasSize(30);
        assertThat(steps)
                .filteredOn(step -> step.getActiveVariable() != null)
                .extracting(step -> step.getActiveVariable().getName())
                .contains("n", "i", "j");
        assertThat(steps.getLast().getOutputs().getLast().getValue())
                .isEqualTo(String.join(System.lineSeparator(),
                        "    *",
                        "   ***",
                        "  *****",
                        " *******",
                        "*********") + System.lineSeparator());
    }

    @Test
    void buildStepsSuppressesInvisiblePyramidNoise() throws Exception {
        JavaRuntimeExecutionFlowTracer tracer = new JavaRuntimeExecutionFlowTracer();
        String sourceCode = """
                public class Main {
                    public static void main(String[] args) {
                        int n = 3;

                        for (int i = 1; i <= n; i++) {
                            for (int j = 1; j <= n - i; j++) {
                                System.out.print(" ");
                            }

                            for (int j = 1; j <= 2 * i - 1; j++) {
                                System.out.print("*");
                            }

                            System.out.println();
                        }
                    }
                }
                """;
        String stdout = compileAndRun(tracer, sourceCode);

        List<ExecutionFlowStep> steps = tracer.buildSteps(sourceCode, stdout);
        List<ExecutionFlowStep> outputSteps = steps.stream()
                .filter(step -> "OUTPUT".equals(step.getEventType()))
                .toList();

        assertThat(steps)
                .filteredOn(step -> "LOOP".equals(step.getEventType()))
                .isEmpty();
        assertThat(outputSteps.getFirst().getStepOrder()).isLessThanOrEqualTo(6);
        assertThat(outputSteps.getFirst().getActiveOutput().getValue()).isEqualTo("  *");
        assertThat(outputSteps)
                .allSatisfy(step -> assertThat(step.getActiveOutput().getValue()).isNotBlank());
        assertThat(steps.getLast().getOutputs().getLast().getValue())
                .isEqualTo(String.join(System.lineSeparator(), "  *", " ***", "*****") + System.lineSeparator());
    }

    private String compileAndRun(JavaRuntimeExecutionFlowTracer tracer, String sourceCode) throws Exception {
        Path workspace = Files.createTempDirectory("cobip-trace-run-");
        try {
            Path sourceFile = workspace.resolve("Main.java");
            Files.writeString(sourceFile, tracer.instrument(sourceCode));

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            assertThat(compiler).isNotNull();
            assertThat(compiler.run(null, null, null, sourceFile.toString())).isZero();

            Process process = new ProcessBuilder("java", "-cp", workspace.toString(), "Main")
                    .redirectErrorStream(true)
                    .start();
            String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertThat(process.waitFor()).isZero();
            return stdout;
        } finally {
            FileSystemUtils.deleteRecursively(workspace);
        }
    }
}
