package com.univerliga.analytics.ingestion;

public interface IngestionClient {

    default void start() {
        // TODO subscribe to broker and update read-model tables.
    }
}
