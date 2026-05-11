package com.cobip.domain.template;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TemplateInterviewQuestion {

    @Column(name = "question", nullable = false, length = 1000)
    private String question;

    @Column(name = "answer_hint", nullable = false, length = 1000)
    private String answerHint;

    public static TemplateInterviewQuestion of(String question, String answerHint) {
        return new TemplateInterviewQuestion(question, answerHint == null ? "" : answerHint);
    }
}
