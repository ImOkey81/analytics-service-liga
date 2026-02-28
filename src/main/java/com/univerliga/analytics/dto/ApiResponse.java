package com.univerliga.analytics.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;

public record ApiResponse<T>(T data, Meta meta) {

    public static <T> ApiResponse<T> of(T data, String requestId) {
        return new ApiResponse<>(data, new Meta(requestId, OffsetDateTime.now().toString(), "v1"));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Meta(String requestId, String timestamp, String version) {
    }
}
