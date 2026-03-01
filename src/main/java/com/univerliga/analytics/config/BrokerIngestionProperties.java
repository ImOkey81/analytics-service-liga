package com.univerliga.analytics.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalDate;
import java.util.List;

@ConfigurationProperties(prefix = "analytics.ingestion")
public class BrokerIngestionProperties {

    private Broker broker = new Broker();
    private Projection projection = new Projection();

    public Broker getBroker() {
        return broker;
    }

    public void setBroker(Broker broker) {
        this.broker = broker;
    }

    public Projection getProjection() {
        return projection;
    }

    public void setProjection(Projection projection) {
        this.projection = projection;
    }

    public static class Broker {
        private boolean enabled = true;
        private String exchange = "univerliga.feedback.events";
        private String queue = "univerliga.analytics.feedback.inbox";
        private String dlq = "univerliga.analytics.feedback.dlq";
        private List<String> routingKeys = List.of("feedback.review.created", "feedback.review.updated", "feedback.review.deleted");
        private int maxRetries = 5;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getExchange() {
            return exchange;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public String getQueue() {
            return queue;
        }

        public void setQueue(String queue) {
            this.queue = queue;
        }

        public String getDlq() {
            return dlq;
        }

        public void setDlq(String dlq) {
            this.dlq = dlq;
        }

        public List<String> getRoutingKeys() {
            return routingKeys;
        }

        public void setRoutingKeys(List<String> routingKeys) {
            this.routingKeys = routingKeys;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }
    }

    public static class Projection {
        private LocalDate periodFrom = LocalDate.of(2026, 1, 1);
        private LocalDate periodTo = LocalDate.of(2026, 12, 31);

        public LocalDate getPeriodFrom() {
            return periodFrom;
        }

        public void setPeriodFrom(LocalDate periodFrom) {
            this.periodFrom = periodFrom;
        }

        public LocalDate getPeriodTo() {
            return periodTo;
        }

        public void setPeriodTo(LocalDate periodTo) {
            this.periodTo = periodTo;
        }
    }
}
