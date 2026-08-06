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

    // interview
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "면접 세션을 찾을 수 없습니다."),
    QUESTION_GENERATION_FAILED(HttpStatus.BAD_GATEWAY, "면접 질문 생성에 실패했습니다."),
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "면접 질문을 찾을 수 없습니다."),
    TTS_GENERATION_FAILED(HttpStatus.BAD_GATEWAY, "질문 음성 생성에 실패했습니다."),

    // feedback
    ;

    private final HttpStatus status;
    private final String message;

    public String getCode() {
        return name();
    }
}
