package com.univerliga.analytics.ingestion;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(IngestionClient.class)
public class NoopIngestionClient implements IngestionClient {

    @Override
    public void start() {
        // no-op for modes without ingestion
    }
}
