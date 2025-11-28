package com.grouplead.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ApiErrorResponse(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp,
        List<FieldError> fieldErrors,
        Map<String, Object> details
) {
    public record FieldError(
            String field,
            String message,
            Object rejectedValue
    ) {}

    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(status, error, message, path, LocalDateTime.now(), null, null);
    }

    public static ApiErrorResponse withFieldErrors(int status, String error, String message, String path, List<FieldError> fieldErrors) {
        return new ApiErrorResponse(status, error, message, path, LocalDateTime.now(), fieldErrors, null);
    }

    public static ApiErrorResponse withDetails(int status, String error, String message, String path, Map<String, Object> details) {
        return new ApiErrorResponse(status, error, message, path, LocalDateTime.now(), null, details);
    }
}
