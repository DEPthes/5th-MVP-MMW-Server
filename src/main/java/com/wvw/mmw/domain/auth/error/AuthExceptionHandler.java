package com.wvw.mmw.domain.auth.error;

import com.wvw.mmw.global.exception.ErrorResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(
            AuthException exception
    ) {
        AuthErrorCode errorCode = exception.getErrorCode();
        log.warn("AuthException: {}", errorCode.getCode());

        ErrorResponse response = new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                List.of()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }
}
