package com.univerliga.analytics.ingestion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.univerliga.analytics.config.BrokerIngestionProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "analytics.ingestion.broker", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BrokerIngestionListener {

    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;
    private final BrokerIngestionProperties ingestionProperties;
    private final BrokerIngestionClient ingestionClient;
    private final FeedbackEventIngestionService ingestionService;

    @RabbitListener(queues = "${analytics.ingestion.broker.queue}", containerFactory = "manualAckIngestionFactory")
    public void consume(Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            if (!ingestionClient.isStarted()) {
                channel.basicAck(deliveryTag, false);
                return;
            }

            String routingKey = message.getMessageProperties().getReceivedRoutingKey();
            FeedbackIngestionEvent event = objectMapper.readValue(message.getBody(), FeedbackIngestionEvent.class);
            withRetries(event, routingKey);
            channel.basicAck(deliveryTag, false);
        } catch (Exception ex) {
            rabbitTemplate.convertAndSend("", ingestionProperties.getBroker().getDlq(), message.getBody());
            channel.basicAck(deliveryTag, false);
            log.error("Ingestion event moved to DLQ", ex);
        }
    }

    private void withRetries(FeedbackIngestionEvent event, String routingKey) {
        int retries = Math.max(1, ingestionProperties.getBroker().getMaxRetries());
        RuntimeException last = null;

        for (int attempt = 1; attempt <= retries; attempt++) {
            try {
                FeedbackEventIngestionService.IngestionResult result = ingestionService.process(event, routingKey);
                if (result.duplicate()) {
                    log.debug("Ingestion duplicate ignored eventId={}", result.eventId());
                }
                return;
            } catch (RuntimeException ex) {
                last = ex;
                sleep(attempt);
            }
        }

        if (last == null) {
            throw new IllegalStateException("Unknown ingestion failure");
        }
        throw last;
    }

    private void sleep(int attempt) {
        try {
            Thread.sleep(Math.min(10_000L, attempt * 1_000L));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Ingestion retry interrupted", e);
        }
    }
}
