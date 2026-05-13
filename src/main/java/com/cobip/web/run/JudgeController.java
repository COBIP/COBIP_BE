package com.cobip.web.run;

import com.cobip.domain.run.JudgeService;
import com.cobip.dto.run.JudgeRequest;
import com.cobip.global.common.ApiResponse;
import jakarta.validation.Valid;
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
    public ResponseEntity<?> judge(@RequestBody @Valid JudgeRequest req) throws Exception {
        var result = judgeService.judge(
                req.getLanguage(),
                req.getCode()
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
