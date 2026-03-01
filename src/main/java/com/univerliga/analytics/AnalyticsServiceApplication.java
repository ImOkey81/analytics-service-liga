package com.univerliga.analytics;

import com.univerliga.analytics.config.AnalyticsProperties;
import com.univerliga.analytics.config.BrokerIngestionProperties;
import com.univerliga.analytics.config.ProvisioningProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({ProvisioningProperties.class, AnalyticsProperties.class, BrokerIngestionProperties.class})
public class AnalyticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsServiceApplication.class, args);
    }
}
