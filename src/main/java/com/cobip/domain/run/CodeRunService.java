package com.cobip.domain.run;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Service
public class CodeRunService {

    // Python 코드 실행 + 입력 전달
    public String runPythonWithInput(String code, String input) throws Exception {

        // python 실행 명령
        ProcessBuilder pb = new ProcessBuilder("python", "-c", code);

        // 에러 출력 포함
        pb.redirectErrorStream(true);

        // 프로세스 시작
        Process process = pb.start();

        // 입력값 전달
        process.getOutputStream().write(input.getBytes());
        process.getOutputStream().flush();
        process.getOutputStream().close();

        // 실행 결과 읽기
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
        );

        // 결과 문자열로 변환
        String output = reader.lines().collect(Collectors.joining("\n"));

        // 실행 완료 대기
        process.waitFor();

        // 공백 제거 후 반환
        return output.trim();
    }
}