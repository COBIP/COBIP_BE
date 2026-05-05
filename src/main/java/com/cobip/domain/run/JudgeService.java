package com.cobip.domain.run;

import com.cobip.dto.run.JudgeResponse;
import org.springframework.stereotype.Service;

@Service
public class JudgeService {

    private final CodeRunService codeRunService;

    public JudgeService(CodeRunService codeRunService) {
        this.codeRunService = codeRunService;
    }

    // 코드 실행 후 결과 비교
    public JudgeResponse judge(String code) throws Exception {

        // 테스트 입력/정답 (MVP)
        String input = "1 1";
        String expected = "2";

        // 코드 실행
        String output = codeRunService.runPythonWithInput(code, input);

        // 결과 비교
        boolean success = output.equals(expected);

        return new JudgeResponse(success, output, expected);
    }
}
