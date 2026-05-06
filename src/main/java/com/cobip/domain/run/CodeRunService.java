package com.cobip.domain.run;

import org.springframework.stereotype.Service;

import java.io.*;
import java.util.stream.Collectors;

@Service
public class CodeRunService {

    // Python 코드 실행 + 입력 전달
    public String runPythonWithInput(String code, String input) throws Exception {

        ProcessBuilder pb = new ProcessBuilder("python", "-c", code);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // 입력 전달
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(process.getOutputStream()))) {

            writer.write(input);
            writer.newLine();
            writer.flush();
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
        );

        String output = reader.lines().collect(Collectors.joining("\n"));

        process.waitFor();

        return output.trim();
    }

    // Java 코드 실행 + 입력 전달
    public String runJavaWithInput(String code, String input) throws Exception {

        String dir = System.getProperty("java.io.tmpdir");
        File javaFile = new File(dir, "Main.java");

        // 1. 파일 생성
        try (FileWriter writer = new FileWriter(javaFile)) {
            writer.write(code);
        }

        // 2. 컴파일
        Process compile = new ProcessBuilder("javac", javaFile.getAbsolutePath())
                .redirectErrorStream(true)
                .start();

        BufferedReader compileReader = new BufferedReader(
                new InputStreamReader(compile.getInputStream())
        );

        String compileOutput = compileReader.lines().collect(Collectors.joining("\n"));

        compile.waitFor();

        // 컴파일 에러
        if (compile.exitValue() != 0) {
            return compileOutput.trim();
        }

        // 3. 실행
        Process run = new ProcessBuilder("java", "-cp", dir, "Main")
                .redirectErrorStream(true)
                .start();

        // 입력 전달
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(run.getOutputStream()))) {

            writer.write(input);
            writer.newLine();
            writer.flush();
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(run.getInputStream())
        );

        String output = reader.lines().collect(Collectors.joining("\n"));

        run.waitFor();

        return output.trim();
    }

    // 언어 분기 실행
    public String runWithInput(String language, String code, String input) throws Exception {

        if ("python".equalsIgnoreCase(language)) {
            return runPythonWithInput(code, input);
        } else if ("java".equalsIgnoreCase(language)) {
            return runJavaWithInput(code, input);
        } else {
            throw new IllegalArgumentException("지원하지 않는 언어");
        }
    }
}