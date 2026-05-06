package com.cobip.domain.run;

import org.springframework.stereotype.Service;

import java.io.*;
import java.util.concurrent.TimeUnit;

@Service
public class CodeRunService {

    private static final int TIME_LIMIT = 3; // 실행 시간 제한 (초)
    private static final int MAX_OUTPUT_LENGTH = 1000; // 출력 길이 제한

    // Python 실행
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

        // 🔥 출력 제한 적용
        StringBuilder output = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            if (output.length() + line.length() > MAX_OUTPUT_LENGTH) {
                output.append("\n[출력 제한 초과]");
                break;
            }
            output.append(line).append("\n");
        }

        // 🔥 시간 제한 적용
        boolean finished = process.waitFor(TIME_LIMIT, TimeUnit.SECONDS);

        if (!finished) {
            process.destroyForcibly();
            return "시간 초과";
        }

        return output.toString().trim();
    }

    // Java 실행
    public String runJavaWithInput(String code, String input) throws Exception {

        String dir = System.getProperty("java.io.tmpdir");
        File javaFile = new File(dir, "Main.java");

        // 파일 생성
        try (FileWriter writer = new FileWriter(javaFile)) {
            writer.write(code);
        }

        // 컴파일
        Process compile = new ProcessBuilder("javac", javaFile.getAbsolutePath())
                .redirectErrorStream(true)
                .start();

        BufferedReader compileReader = new BufferedReader(
                new InputStreamReader(compile.getInputStream())
        );

        StringBuilder compileOutput = new StringBuilder();
        String line;

        while ((line = compileReader.readLine()) != null) {
            compileOutput.append(line).append("\n");
        }

        compile.waitFor();

        // 컴파일 실패
        if (compile.exitValue() != 0) {
            return compileOutput.toString().trim();
        }

        // 실행
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

        // 🔥 출력 제한 적용
        StringBuilder output = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            if (output.length() + line.length() > MAX_OUTPUT_LENGTH) {
                output.append("\n[출력 제한 초과]");
                break;
            }
            output.append(line).append("\n");
        }

        // 🔥 시간 제한 적용
        boolean finished = run.waitFor(TIME_LIMIT, TimeUnit.SECONDS);

        if (!finished) {
            run.destroyForcibly();
            return "시간 초과";
        }

        return output.toString().trim();
    }

    // 언어 분기
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