package com.univerliga.analytics.ingestion;

import com.univerliga.analytics.config.ProvisioningProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "analytics.ingestion.broker", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BrokerIngestionClient implements IngestionClient {

    private final ProvisioningProperties provisioningProperties;
    private volatile boolean started;

    @Override
    public synchronized void start() {
        started = provisioningProperties.mode() == ProvisioningProperties.Mode.readmodel;
    }

    public boolean isStarted() {
        return started;
    }
}
