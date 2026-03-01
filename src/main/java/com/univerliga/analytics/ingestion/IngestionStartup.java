package com.univerliga.analytics.ingestion;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class IngestionStartup {

    private final IngestionClient ingestionClient;

    @Bean
    ApplicationRunner startIngestionRunner() {
        return args -> ingestionClient.start();
    }
}
