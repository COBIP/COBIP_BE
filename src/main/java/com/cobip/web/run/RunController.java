package com.cobip.web.run;

import com.cobip.domain.run.CodeRunService;
import com.cobip.dto.run.RunRequest;
import com.cobip.dto.run.RunResponse;
import com.cobip.global.common.ApiResponse;
import jakarta.validation.Valid;
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
    public ResponseEntity<?> run(@RequestBody @Valid RunRequest req) throws Exception {
        String output = codeRunService.runWithInput(
                req.getLanguage(),
                req.getCode(),
                "" // run은 입력 없이 실행 (MVP)
        );
        return ResponseEntity.ok(ApiResponse.success(new RunResponse(output)));
    }
}
