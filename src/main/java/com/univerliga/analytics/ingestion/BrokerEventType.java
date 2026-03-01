package com.univerliga.analytics.ingestion;

public enum BrokerEventType {
    FeedbackCreated,
    FeedbackUpdated,
    FeedbackDeleted;

    public boolean isDelete() {
        return this == FeedbackDeleted;
    }
}
