package com.univerliga.analytics.controller;

import com.univerliga.analytics.config.ProvisioningProperties;
import com.univerliga.analytics.dto.ApiResponse;
import com.univerliga.analytics.dto.SystemVersionResponse;
import com.univerliga.analytics.util.RequestIdHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/system", produces = "application/json; charset=utf-8")
public class SystemController {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${info.app.version:0.1.0}")
    private String version;

    private final ProvisioningProperties provisioningProperties;

    public SystemController(ProvisioningProperties provisioningProperties) {
        this.provisioningProperties = provisioningProperties;
    }

    @GetMapping("/version")
    @Operation(summary = "Service version", responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"data\":{\"name\":\"univerliga-analytics-service\",\"version\":\"0.1.0\",\"mode\":\"mock\"},\"meta\":{\"requestId\":\"req-1\",\"timestamp\":\"2026-01-01T10:00:00Z\",\"version\":\"v1\"}}"))))
    public ApiResponse<SystemVersionResponse> version() {
        return ApiResponse.of(new SystemVersionResponse(appName, version, provisioningProperties.mode().name()), RequestIdHolder.get());
    }
}
