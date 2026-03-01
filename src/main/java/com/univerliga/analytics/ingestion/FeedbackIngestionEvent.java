package com.univerliga.analytics.ingestion;

import java.time.OffsetDateTime;

public record FeedbackIngestionEvent(
        String eventId,
        BrokerEventType type,
        OffsetDateTime occurredAt,
        String source,
        FeedbackEventPayload payload
) {
}
