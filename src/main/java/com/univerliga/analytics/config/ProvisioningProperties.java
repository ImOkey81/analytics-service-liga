package com.univerliga.analytics.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "provisioning")
public record ProvisioningProperties(@NotNull Mode mode) {

    public enum Mode {
        mock,
        readmodel
    }
}
