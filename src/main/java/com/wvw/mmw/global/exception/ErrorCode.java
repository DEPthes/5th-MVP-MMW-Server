package com.wvw.mmw.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INVALID_INPUT_VALUE(
            HttpStatus.BAD_REQUEST,
            "입력값 검증에 실패했습니다."
    ),

    PASSWORD_MISMATCH(
            HttpStatus.BAD_REQUEST,
            "비밀번호와 비밀번호 확인이 일치하지 않습니다."
    ),

    PRIVACY_CONSENT_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "개인정보 수집에 동의해야 회원가입할 수 있습니다."
    ),

    EMAIL_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "이미 사용 중인 이메일입니다."
    ),

    LOGIN_ID_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "이미 사용 중인 아이디입니다."
    ),

    DUPLICATE_MEMBER_DATA(
            HttpStatus.CONFLICT,
            "이미 가입된 이메일 또는 아이디입니다."
    ),

    INVALID_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "아이디 또는 비밀번호가 올바르지 않습니다."
    ),

    INVALID_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "유효하지 않거나 만료된 인증 토큰입니다."
    ),

    INVALID_REFRESH_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "유효하지 않거나 만료된 Refresh Token입니다."
    ),

    INVALID_CURRENT_PASSWORD(
            HttpStatus.BAD_REQUEST,
            "현재 비밀번호가 일치하지 않습니다."
    ),

    SAME_AS_CURRENT_PASSWORD(
            HttpStatus.BAD_REQUEST,
            "새 비밀번호는 현재 비밀번호와 달라야 합니다."
    ),

    ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "접근 권한이 없습니다."
    ),

    RESOURCE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "요청한 리소스를 찾을 수 없습니다."
    ),

    METHOD_NOT_ALLOWED(
            HttpStatus.METHOD_NOT_ALLOWED,
            "지원하지 않는 HTTP 메서드입니다."
    ),

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "서버 내부 오류가 발생했습니다."
    );

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
