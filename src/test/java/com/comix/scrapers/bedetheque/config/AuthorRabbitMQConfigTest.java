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
class AuthorRabbitMQConfigTest {

    @InjectMocks
    private AuthorRabbitMQConfig authorRabbitMQConfig;

    // Test values for the configuration properties
    private final String authorQueueName = "test.author.queue";
    private final String authorExchangeName = "test.author.exchange";
    private final String authorDlqName = "test.author.dlq";
    private final String authorDlxName = "test.author.dlx";
    private final String authorRetryQueueName = "test.author.retry.queue";
    private final String authorRetryExchangeName = "test.author.retry.exchange";
    private final int authorRetryTtl = 5000;

    @BeforeEach
    void setUp() {
        // Use ReflectionTestUtils to inject test values into the @Value annotated fields
        ReflectionTestUtils.setField(authorRabbitMQConfig, "authorQueue", authorQueueName);
        ReflectionTestUtils.setField(authorRabbitMQConfig, "authorExchange", authorExchangeName);
        ReflectionTestUtils.setField(authorRabbitMQConfig, "authorDlq", authorDlqName);
        ReflectionTestUtils.setField(authorRabbitMQConfig, "authorDlx", authorDlxName);
        ReflectionTestUtils.setField(authorRabbitMQConfig, "authorRetryQueue", authorRetryQueueName);
        ReflectionTestUtils.setField(authorRabbitMQConfig, "authorRetryExchange", authorRetryExchangeName);
        ReflectionTestUtils.setField(authorRabbitMQConfig, "authorRetryTtl", authorRetryTtl);
    }

    @Test
    @DisplayName("should create authorExchange bean correctly")
    void shouldCreateAuthorExchange() {
        // When
        DirectExchange exchange = authorRabbitMQConfig.authorExchange();

        // Then
        assertThat(exchange.getName()).isEqualTo(authorExchangeName);
    }

    @Test
    @DisplayName("should create deadLetterExchange bean correctly")
    void shouldCreateDeadLetterExchange() {
        // When
        DirectExchange exchange = authorRabbitMQConfig.deadLetterExchange();

        // Then
        assertThat(exchange.getName()).isEqualTo(authorDlxName);
    }

    @Test
    @DisplayName("should create retryDeadLetterExchange bean correctly")
    void shouldCreateRetryDeadLetterExchange() {
        // When
        DirectExchange exchange = authorRabbitMQConfig.retryDeadLetterExchange();

        // Then
        assertThat(exchange.getName()).isEqualTo(authorRetryExchangeName);
    }

    @Test
    @DisplayName("should create authorQueue bean with dead-letter argument")
    void shouldCreateAuthorQueue() {
        // When
        Queue queue = authorRabbitMQConfig.authorQueue();

        // Then
        assertThat(queue.getName()).isEqualTo(authorQueueName);
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.getArguments()).containsEntry("x-dead-letter-exchange", authorRetryExchangeName);
    }

    @Test
    @DisplayName("should create retryQueue bean with TTL and dead-letter arguments")
    void shouldCreateRetryQueue() {
        // When
        Queue queue = authorRabbitMQConfig.retryQueue();

        // Then
        assertThat(queue.getName()).isEqualTo(authorRetryQueueName);
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.getArguments()).containsEntry("x-dead-letter-exchange", authorExchangeName);
        assertThat(queue.getArguments()).containsEntry("x-message-ttl", authorRetryTtl);
    }

    @Test
    @DisplayName("should create deadLetterQueue bean correctly")
    void shouldCreateDeadLetterQueue() {
        // When
        Queue queue = authorRabbitMQConfig.deadLetterQueue();

        // Then
        assertThat(queue.getName()).isEqualTo(authorDlqName);
        assertThat(queue.isDurable()).isTrue();
    }

    @Test
    @DisplayName("should create authorBinding correctly")
    void shouldCreateAuthorBinding() {
        // Given
        Queue queue = authorRabbitMQConfig.authorQueue();
        DirectExchange exchange = authorRabbitMQConfig.authorExchange();

        // When
        Binding binding = authorRabbitMQConfig.authorBinding(queue, exchange);

        // Then
        assertThat(binding.getDestination()).isEqualTo(authorQueueName);
        assertThat(binding.getExchange()).isEqualTo(authorExchangeName);
        assertThat(binding.getRoutingKey()).isEqualTo(authorQueueName);
    }

    @Test
    @DisplayName("should create retryBinding correctly")
    void shouldCreateRetryBinding() {
        // Given
        Queue queue = authorRabbitMQConfig.retryQueue();
        DirectExchange exchange = authorRabbitMQConfig.retryDeadLetterExchange();

        // When
        Binding binding = authorRabbitMQConfig.retryBinding(queue, exchange);

        // Then
        assertThat(binding.getDestination()).isEqualTo(authorRetryQueueName);
        assertThat(binding.getExchange()).isEqualTo(authorRetryExchangeName);
        assertThat(binding.getRoutingKey()).isEqualTo(authorQueueName);
    }

    @Test
    @DisplayName("should create dlqBinding correctly")
    void shouldCreateDlqBinding() {
        // Given
        Queue queue = authorRabbitMQConfig.deadLetterQueue();
        DirectExchange exchange = authorRabbitMQConfig.deadLetterExchange();

        // When
        Binding binding = authorRabbitMQConfig.dlqBinding(queue, exchange);

        // Then
        assertThat(binding.getDestination()).isEqualTo(authorDlqName);
        assertThat(binding.getExchange()).isEqualTo(authorDlxName);
        assertThat(binding.getRoutingKey()).isEqualTo(authorQueueName);
    }
}