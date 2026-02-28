package com.univerliga.analytics.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "analytics")
public record AnalyticsProperties(Features features) {

    public record Features(boolean network) {
    }
}
