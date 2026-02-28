package com.univerliga.analytics.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String code;
    private final List<ApiErrorResponse.ErrorDetail> details;

    public ApiException(HttpStatus status, String code, String message) {
        this(status, code, message, List.of());
    }

    public ApiException(HttpStatus status, String code, String message, List<ApiErrorResponse.ErrorDetail> details) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details;
    }
}
