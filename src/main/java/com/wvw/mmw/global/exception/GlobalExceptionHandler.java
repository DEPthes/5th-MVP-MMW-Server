package com.wvw.mmw.global.exception;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("BusinessException: {}", errorCode.getCode());
        return toResponse(errorCode);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<ErrorResponse.FieldError> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ErrorResponse.FieldError(
                        fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();
        log.warn("MethodArgumentNotValidException: {}", errors);
        return toResponse(ErrorCode.INVALID_INPUT_VALUE, errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException e) {
        List<ErrorResponse.FieldError> errors = e.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> new ErrorResponse.FieldError(
                                result.getMethodParameter().getParameterName(), error.getDefaultMessage())))
                .toList();
        log.warn("HandlerMethodValidationException: {}", errors);
        return toResponse(ErrorCode.INVALID_INPUT_VALUE, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadableException: {}", e.getMessage());
        return toResponse(ErrorCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        List<ErrorResponse.FieldError> errors = List.of(
                new ErrorResponse.FieldError(e.getName(), "올바르지 않은 값입니다: " + e.getValue()));
        log.warn("MethodArgumentTypeMismatchException: {}", errors);
        return toResponse(ErrorCode.INVALID_TYPE_VALUE, errors);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("NoResourceFoundException: {}", e.getResourcePath());
        return toResponse(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("HttpRequestMethodNotSupportedException: {}", e.getMethod());
        return toResponse(ErrorCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        log.warn("MaxUploadSizeExceededException: {}", e.getMessage());
        return toResponse(ErrorCode.FILE_SIZE_EXCEEDED);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("DataIntegrityViolationException", e);
        return toResponse(ErrorCode.DATA_INTEGRITY_VIOLATION);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return toResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> toResponse(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatus()).body(ErrorResponse.of(errorCode));
    }

    private ResponseEntity<ErrorResponse> toResponse(ErrorCode errorCode,
                                                    List<ErrorResponse.FieldError> errors) {
        return ResponseEntity.status(errorCode.getStatus()).body(ErrorResponse.of(errorCode, errors));
    }
}
