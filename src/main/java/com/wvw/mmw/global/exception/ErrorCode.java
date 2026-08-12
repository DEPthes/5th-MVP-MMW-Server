package com.wvw.mmw.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {


    // 공통
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값 검증에 실패했습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "요청 값의 타입이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "요청이 데이터 제약 조건에 위배됩니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.CONTENT_TOO_LARGE, "파일 크기가 허용 범위를 초과했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // profile
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "지원 프로필을 찾을 수 없습니다."),
    PROFILE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 지원 프로필에 접근할 수 없습니다."),

    // user
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    INVALID_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "현재 비밀번호가 올바르지 않습니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    SAME_AS_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "새 비밀번호는 현재 비밀번호와 달라야 합니다."),
    PRIVACY_CONSENT_REQUIRED(HttpStatus.BAD_REQUEST, "개인정보 수집 및 이용에 동의해야 합니다."),
    LOGIN_ID_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // interview
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "면접 세션을 찾을 수 없습니다."),
    QUESTION_GENERATION_FAILED(HttpStatus.BAD_GATEWAY, "면접 질문 생성에 실패했습니다."),

    // feedback
    INTERVIEW_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND,"존재하지 않는 면접 세션"),
    INTERVIEW_QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 면접 질문"),
    ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND,"해당 질문에 대한 답변 존재하지 않음"),
    OVERALL_FEEDBACK_NOT_FOUND(HttpStatus.NOT_FOUND, "생성되지 않은 종합피드백"),
    FEEDBACK_ALREADY_EXIST(HttpStatus.CONFLICT,"이지 존재하는 피드백"),
    AI_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"AI 서버 통신 실패"),
    AI_OVERALL_FEEDBACK_PARSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI 종합 피드백 파싱 중 오류 발생"),
    INTERVIEW_NOT_COMPLETED(HttpStatus.BAD_REQUEST,"면접이 종료되지 않음"),
    INTERVIEW_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST,"이미 종료된 면접 세션"),



    ;

    private final HttpStatus status;
    private final String message;

    public String getCode() {
        return name();
    }
}
