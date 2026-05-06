package com.cobip.web.run;

import com.cobip.domain.run.JudgeService;
import com.cobip.dto.run.JudgeRequest;
import com.cobip.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/judge")
public class JudgeController {

    private final JudgeService judgeService;

    // 코드 채점 API
    @PostMapping
    public ResponseEntity<?> judge(@RequestBody JudgeRequest req) {

        try {
            var result = judgeService.judge(
                    req.getLanguage(),
                    req.getCode()
            );

            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (Exception e) {
            return ResponseEntity
                    .status(500)
                    .body(ApiResponse.error("채점 및 코드 실행 실패"));
        }
    }
}