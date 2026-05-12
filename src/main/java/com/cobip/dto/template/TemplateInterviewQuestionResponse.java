package com.cobip.dto.template;

import com.cobip.domain.template.TemplateInterviewQuestion;

import lombok.Getter;

@Getter
public class TemplateInterviewQuestionResponse {

    private final String question;
    private final String answerHint;

    private TemplateInterviewQuestionResponse(TemplateInterviewQuestion interviewQuestion) {
        this.question = interviewQuestion.getQuestion();
        this.answerHint = interviewQuestion.getAnswerHint();
    }

    public static TemplateInterviewQuestionResponse from(TemplateInterviewQuestion interviewQuestion) {
        return new TemplateInterviewQuestionResponse(interviewQuestion);
    }
}
