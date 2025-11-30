package com.comix.scrapers.bedetheque.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SerieRabbitMQConfigTest {

    @InjectMocks
    private SerieRabbitMQConfig serieRabbitMQConfig;

    // Test values for the configuration properties
    private final String serieQueueName = "test.serie.queue";
    private final String serieExchangeName = "test.serie.exchange";
    private final String serieDlqName = "test.serie.dlq";
    private final String serieDlxName = "test.serie.dlx";
    private final String serieRetryQueueName = "test.serie.retry.queue";
    private final String serieRetryExchangeName = "test.serie.retry.exchange";
    private final int serieRetryTtl = 5000;

    @BeforeEach
    void setUp() {
        // Use ReflectionTestUtils to inject test values into the @Value annotated fields
        ReflectionTestUtils.setField(serieRabbitMQConfig, "serieQueue", serieQueueName);
        ReflectionTestUtils.setField(serieRabbitMQConfig, "serieExchange", serieExchangeName);
        ReflectionTestUtils.setField(serieRabbitMQConfig, "serieDlq", serieDlqName);
        ReflectionTestUtils.setField(serieRabbitMQConfig, "serieDlx", serieDlxName);
        ReflectionTestUtils.setField(serieRabbitMQConfig, "serieRetryQueue", serieRetryQueueName);
        ReflectionTestUtils.setField(serieRabbitMQConfig, "serieRetryExchange", serieRetryExchangeName);
        ReflectionTestUtils.setField(serieRabbitMQConfig, "serieRetryTtl", serieRetryTtl);
    }

    @Test
    @DisplayName("should create serieExchange bean correctly")
    void shouldCreateSerieExchange() {
        // When
        DirectExchange exchange = serieRabbitMQConfig.serieExchange();

        // Then
        assertThat(exchange.getName()).isEqualTo(serieExchangeName);
    }

    @Test
    @DisplayName("should create serieDeadLetterExchange bean correctly")
    void shouldCreateSerieDeadLetterExchange() {
        // When
        DirectExchange exchange = serieRabbitMQConfig.serieDeadLetterExchange();

        // Then
        assertThat(exchange.getName()).isEqualTo(serieDlxName);
    }

    @Test
    @DisplayName("should create serieRetryDeadLetterExchange bean correctly")
    void shouldCreateSerieRetryDeadLetterExchange() {
        // When
        DirectExchange exchange = serieRabbitMQConfig.serieRetryDeadLetterExchange();

        // Then
        assertThat(exchange.getName()).isEqualTo(serieRetryExchangeName);
    }

    @Test
    @DisplayName("should create serieQueue bean with dead-letter argument")
    void shouldCreateSerieQueue() {
        // When
        Queue queue = serieRabbitMQConfig.serieQueue();

        // Then
        assertThat(queue.getName()).isEqualTo(serieQueueName);
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.getArguments()).containsEntry("x-dead-letter-exchange", serieRetryExchangeName);
    }

    @Test
    @DisplayName("should create serieRetryQueue bean with TTL and dead-letter arguments")
    void shouldCreateSerieRetryQueue() {
        // When
        Queue queue = serieRabbitMQConfig.serieRetryQueue();

        // Then
        assertThat(queue.getName()).isEqualTo(serieRetryQueueName);
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.getArguments()).containsEntry("x-dead-letter-exchange", serieExchangeName);
        assertThat(queue.getArguments()).containsEntry("x-message-ttl", serieRetryTtl);
    }

    @Test
    @DisplayName("should create serieDeadLetterQueue bean correctly")
    void shouldCreateSerieDeadLetterQueue() {
        // When
        Queue queue = serieRabbitMQConfig.serieDeadLetterQueue();

        // Then
        assertThat(queue.getName()).isEqualTo(serieDlqName);
        assertThat(queue.isDurable()).isTrue();
    }

    @Test
    @DisplayName("should create serieBinding correctly")
    void shouldCreateSerieBinding() {
        // Given
        Queue queue = serieRabbitMQConfig.serieQueue();
        DirectExchange exchange = serieRabbitMQConfig.serieExchange();

        // When
        Binding binding = serieRabbitMQConfig.serieBinding(queue, exchange);

        // Then
        assertThat(binding.getDestination()).isEqualTo(serieQueueName);
        assertThat(binding.getExchange()).isEqualTo(serieExchangeName);
        assertThat(binding.getRoutingKey()).isEqualTo(serieQueueName);
    }

    @Test
    @DisplayName("should create serieRetryBinding correctly")
    void shouldCreateSerieRetryBinding() {
        // Given
        Queue queue = serieRabbitMQConfig.serieRetryQueue();
        DirectExchange exchange = serieRabbitMQConfig.serieRetryDeadLetterExchange();

        // When
        Binding binding = serieRabbitMQConfig.serieRetryBinding(queue, exchange);

        // Then
        assertThat(binding.getDestination()).isEqualTo(serieRetryQueueName);
        assertThat(binding.getExchange()).isEqualTo(serieRetryExchangeName);
        assertThat(binding.getRoutingKey()).isEqualTo(serieQueueName);
    }

    @Test
    @DisplayName("should create serieDlqBinding correctly")
    void shouldCreateSerieDlqBinding() {
        // Given
        Queue queue = serieRabbitMQConfig.serieDeadLetterQueue();
        DirectExchange exchange = serieRabbitMQConfig.serieDeadLetterExchange();

        // When
        Binding binding = serieRabbitMQConfig.serieDlqBinding(queue, exchange);

        // Then
        assertThat(binding.getDestination()).isEqualTo(serieDlqName);
        assertThat(binding.getExchange()).isEqualTo(serieDlxName);
        assertThat(binding.getRoutingKey()).isEqualTo(serieQueueName);
    }
}