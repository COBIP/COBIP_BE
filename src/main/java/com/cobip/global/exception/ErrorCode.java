package com.cobip.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "템플릿을 찾을 수 없습니다."),
    GRAMMAR_TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "문법 템플릿을 찾을 수 없습니다."),
    CODING_PROBLEM_NOT_FOUND(HttpStatus.NOT_FOUND, "코딩 문제를 찾을 수 없습니다."),
    SUBSCRIPTION_REQUIRED(HttpStatus.FORBIDDEN, "구독이 필요한 템플릿입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
    DUPLICATE_FAVORITE(HttpStatus.CONFLICT, "이미 찜한 템플릿입니다."),
    DUPLICATE_GRAMMAR_TEMPLATE_SLUG(HttpStatus.CONFLICT, "이미 존재하는 문법 템플릿 slug입니다."),
    DUPLICATE_SUBSCRIPTION_PLAN_CODE(HttpStatus.CONFLICT, "이미 존재하는 구독 플랜 코드입니다."),
    ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "계정이 활성 상태가 아닙니다."),
    EMAIL_VERIFICATION_REQUIRED(HttpStatus.BAD_REQUEST, "이메일 인증이 필요합니다."),
    EMAIL_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "이메일 인증 코드가 올바르지 않거나 만료되었습니다."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 발송에 실패했습니다."),
    SUBSCRIPTION_PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "구독 플랜을 찾을 수 없습니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "신고를 찾을 수 없습니다."),
    CERTIFICATE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "수료 조건을 충족하지 않았습니다."),
    CERTIFICATE_ALREADY_ISSUED(HttpStatus.CONFLICT, "이미 발급된 수료증입니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
