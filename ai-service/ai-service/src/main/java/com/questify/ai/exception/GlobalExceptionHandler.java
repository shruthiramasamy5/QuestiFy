package com.questify.ai.exception;

import com.questify.ai.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> details = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            details.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "Validation Failed", "Request validation failed", details));
    }

    @ExceptionHandler(AiProviderNotConfiguredException.class)
    public ResponseEntity<ApiError> handleNotConfigured(AiProviderNotConfiguredException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiError.of(503, "AI Provider Not Configured", ex.getMessage(), Map.of()));
    }

    @ExceptionHandler(AiUpstreamException.class)
    public ResponseEntity<ApiError> handleUpstream(AiUpstreamException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiError.of(502, "AI Upstream Error", ex.getMessage(), Map.of()));
    }

    @ExceptionHandler(AiResponseValidationException.class)
    public ResponseEntity<ApiError> handleInvalidAiResponse(AiResponseValidationException ex) {
        return ResponseEntity.unprocessableEntity()
                .body(ApiError.of(422, "Invalid AI Response", ex.getMessage(), Map.of()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.of(403, "Forbidden", "You are not allowed to perform this action", Map.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of(500, "Internal Server Error", ex.getMessage(), Map.of()));
    }
}
