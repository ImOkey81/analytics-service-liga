package com.univerliga.analytics.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConditionalOnProperty(prefix = "analytics.ingestion.broker", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RabbitIngestionConfig {

    @Bean
    TopicExchange analyticsIngestionExchange(BrokerIngestionProperties props) {
        return new TopicExchange(props.getBroker().getExchange(), true, false);
    }

    @Bean
    Queue analyticsIngestionQueue(BrokerIngestionProperties props) {
        return QueueBuilder.durable(props.getBroker().getQueue())
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", props.getBroker().getDlq())
                .build();
    }

    @Bean
    Queue analyticsIngestionDlq(BrokerIngestionProperties props) {
        return QueueBuilder.durable(props.getBroker().getDlq()).build();
    }

    @Bean
    Declarables analyticsIngestionBindings(BrokerIngestionProperties props, TopicExchange analyticsIngestionExchange, Queue analyticsIngestionQueue) {
        List<Binding> bindings = props.getBroker().getRoutingKeys().stream()
                .map(String::trim)
                .map(routingKey -> BindingBuilder.bind(analyticsIngestionQueue).to(analyticsIngestionExchange).with(routingKey))
                .toList();
        return new Declarables(bindings);
    }

    @Bean
    SimpleRabbitListenerContainerFactory manualAckIngestionFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
