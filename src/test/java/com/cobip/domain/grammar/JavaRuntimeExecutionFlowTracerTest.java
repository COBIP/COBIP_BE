package com.cobip.domain.grammar;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

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
}
