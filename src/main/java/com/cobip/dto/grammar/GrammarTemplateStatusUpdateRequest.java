package com.cobip.dto.grammar;

import com.cobip.domain.grammar.GrammarTemplateStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GrammarTemplateStatusUpdateRequest {

    @NotNull(message = "공개 상태는 필수입니다.")
    private GrammarTemplateStatus status;
}
