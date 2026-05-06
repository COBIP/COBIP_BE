package com.cobip.web.run;

import com.cobip.domain.run.CodeRunService;
import com.cobip.dto.run.RunRequest;
import com.cobip.dto.run.RunResponse;
import com.cobip.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/run")
public class RunController {

    private final CodeRunService codeRunService;

    // 코드 실행 API
    @PostMapping
    public ResponseEntity<?> run(@RequestBody RunRequest req) {

        try {
            // 🔥 언어 분기 실행
            String output = codeRunService.runWithInput(
                    req.getLanguage(),
                    req.getCode(),
                    "" // run은 입력 없이 실행 (MVP)
            );

            return ResponseEntity.ok(
                    ApiResponse.success(new RunResponse(output))
            );

        } catch (Exception e) {
            return ResponseEntity
                    .status(500)
                    .body(ApiResponse.error("코드 실행 실패"));
        }
    }
}