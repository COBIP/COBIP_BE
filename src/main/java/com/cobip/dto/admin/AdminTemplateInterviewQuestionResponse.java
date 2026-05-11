package com.cobip.dto.admin;

import com.cobip.domain.template.TemplateInterviewQuestion;

import lombok.Getter;

@Getter
public class AdminTemplateInterviewQuestionResponse {

    private final String question;
    private final String answerHint;

    private AdminTemplateInterviewQuestionResponse(TemplateInterviewQuestion interviewQuestion) {
        this.question = interviewQuestion.getQuestion();
        this.answerHint = interviewQuestion.getAnswerHint();
    }

    public static AdminTemplateInterviewQuestionResponse from(TemplateInterviewQuestion interviewQuestion) {
        return new AdminTemplateInterviewQuestionResponse(interviewQuestion);
    }
}
