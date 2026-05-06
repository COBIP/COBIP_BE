package com.cobip.domain.run;

import com.cobip.dto.run.JudgeResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JudgeService {

    private final CodeRunService codeRunService;

    public JudgeService(CodeRunService codeRunService) {
        this.codeRunService = codeRunService;
    }

    // 여러 테스트케이스로 채점 (언어 공통)
    public JudgeResponse judge(String language, String code) throws Exception {

        // 테스트케이스 목록
        List<String[]> testCases = List.of(
                new String[]{"1 1", "2"},
                new String[]{"2 3", "5"},
                new String[]{"10 20", "30"}
        );

        boolean allPass = true;
        String lastOutput = "";
        String expected = "";

        for (String[] tc : testCases) {

            String input = tc[0];
            expected = tc[1];

            // 🔥 언어별 실행 (핵심)
            String output = codeRunService.runWithInput(language, code, input);

            lastOutput = output;

            // 하나라도 틀리면 실패
            if (!output.equals(expected)) {
                allPass = false;
                break;
            }
        }

        return new JudgeResponse(allPass, lastOutput, expected);
    }
}