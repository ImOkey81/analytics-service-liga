package com.univerliga.analytics.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public record ApiErrorResponse(ErrorBody error) {

    public static ApiErrorResponse of(String code, String message, List<ErrorDetail> details, String requestId) {
        return new ApiErrorResponse(new ErrorBody(code, message, details, requestId));
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public record ErrorBody(String code, String message, List<ErrorDetail> details, String requestId) {
    }

    public record ErrorDetail(String field, String issue) {
    }
}
