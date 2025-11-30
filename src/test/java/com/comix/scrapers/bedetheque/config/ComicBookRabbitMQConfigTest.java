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
class ComicBookRabbitMQConfigTest {

    @InjectMocks
    private ComicBookRabbitMQConfig comicBookRabbitMQConfig;

    // Test values for the configuration properties
    private final String comicBookQueueName = "test.comic-book.queue";
    private final String comicBookExchangeName = "test.comic-book.exchange";
    private final String comicBookDlqName = "test.comic-book.dlq";
    private final String comicBookDlxName = "test.comic-book.dlx";
    private final String comicBookRetryQueueName = "test.comic-book.retry.queue";
    private final String comicBookRetryExchangeName = "test.comic-book.retry.exchange";
    private final int comicBookRetryTtl = 5000;

    @BeforeEach
    void setUp() {
        // Use ReflectionTestUtils to inject test values into the @Value annotated fields
        ReflectionTestUtils.setField(comicBookRabbitMQConfig, "comicBookQueue", comicBookQueueName);
        ReflectionTestUtils.setField(comicBookRabbitMQConfig, "comicBookExchange", comicBookExchangeName);
        ReflectionTestUtils.setField(comicBookRabbitMQConfig, "comicBookDlq", comicBookDlqName);
        ReflectionTestUtils.setField(comicBookRabbitMQConfig, "comicBookDlx", comicBookDlxName);
        ReflectionTestUtils.setField(comicBookRabbitMQConfig, "comicBookRetryQueue", comicBookRetryQueueName);
        ReflectionTestUtils.setField(comicBookRabbitMQConfig, "comicBookRetryExchange", comicBookRetryExchangeName);
        ReflectionTestUtils.setField(comicBookRabbitMQConfig, "comicBookRetryTtl", comicBookRetryTtl);
    }

    @Test
    @DisplayName("should create comicBookExchange bean correctly")
    void shouldCreateComicBookExchange() {
        // When
        DirectExchange exchange = comicBookRabbitMQConfig.comicBookExchange();

        // Then
        assertThat(exchange.getName()).isEqualTo(comicBookExchangeName);
    }

    @Test
    @DisplayName("should create comicBookDeadLetterExchange bean correctly")
    void shouldCreateComicBookDeadLetterExchange() {
        // When
        DirectExchange exchange = comicBookRabbitMQConfig.comicBookDeadLetterExchange();

        // Then
        assertThat(exchange.getName()).isEqualTo(comicBookDlxName);
    }

    @Test
    @DisplayName("should create comicBookRetryDeadLetterExchange bean correctly")
    void shouldCreateComicBookRetryDeadLetterExchange() {
        // When
        DirectExchange exchange = comicBookRabbitMQConfig.comicBookRetryDeadLetterExchange();

        // Then
        assertThat(exchange.getName()).isEqualTo(comicBookRetryExchangeName);
    }

    @Test
    @DisplayName("should create comicBookQueue bean with dead-letter argument")
    void shouldCreateComicBookQueue() {
        // When
        Queue queue = comicBookRabbitMQConfig.comicBookQueue();

        // Then
        assertThat(queue.getName()).isEqualTo(comicBookQueueName);
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.getArguments()).containsEntry("x-dead-letter-exchange", comicBookRetryExchangeName);
    }

    @Test
    @DisplayName("should create comicBookRetryQueue bean with TTL and dead-letter arguments")
    void shouldCreateComicBookRetryQueue() {
        // When
        Queue queue = comicBookRabbitMQConfig.comicBookRetryQueue();

        // Then
        assertThat(queue.getName()).isEqualTo(comicBookRetryQueueName);
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.getArguments()).containsEntry("x-dead-letter-exchange", comicBookExchangeName);
        assertThat(queue.getArguments()).containsEntry("x-message-ttl", comicBookRetryTtl);
    }

    @Test
    @DisplayName("should create comicBookDeadLetterQueue bean correctly")
    void shouldCreateComicBookDeadLetterQueue() {
        // When
        Queue queue = comicBookRabbitMQConfig.comicBookDeadLetterQueue();

        // Then
        assertThat(queue.getName()).isEqualTo(comicBookDlqName);
        assertThat(queue.isDurable()).isTrue();
    }

    @Test
    @DisplayName("should create comicBookBinding correctly")
    void shouldCreateComicBookBinding() {
        // Given
        Queue queue = comicBookRabbitMQConfig.comicBookQueue();
        DirectExchange exchange = comicBookRabbitMQConfig.comicBookExchange();

        // When
        Binding binding = comicBookRabbitMQConfig.comicBookBinding(queue, exchange);

        // Then
        assertThat(binding.getDestination()).isEqualTo(comicBookQueueName);
        assertThat(binding.getExchange()).isEqualTo(comicBookExchangeName);
        assertThat(binding.getRoutingKey()).isEqualTo(comicBookQueueName);
    }

    @Test
    @DisplayName("should create comicBookRetryBinding correctly")
    void shouldCreateComicBookRetryBinding() {
        // Given
        Queue queue = comicBookRabbitMQConfig.comicBookRetryQueue();
        DirectExchange exchange = comicBookRabbitMQConfig.comicBookRetryDeadLetterExchange();

        // When
        Binding binding = comicBookRabbitMQConfig.comicBookRetryBinding(queue, exchange);

        // Then
        assertThat(binding.getDestination()).isEqualTo(comicBookRetryQueueName);
        assertThat(binding.getExchange()).isEqualTo(comicBookRetryExchangeName);
        assertThat(binding.getRoutingKey()).isEqualTo(comicBookQueueName);
    }

    @Test
    @DisplayName("should create comicBookDlqBinding correctly")
    void shouldCreateComicBookDlqBinding() {
        // Given
        Queue queue = comicBookRabbitMQConfig.comicBookDeadLetterQueue();
        DirectExchange exchange = comicBookRabbitMQConfig.comicBookDeadLetterExchange();

        // When
        Binding binding = comicBookRabbitMQConfig.comicBookDlqBinding(queue, exchange);

        // Then
        assertThat(binding.getDestination()).isEqualTo(comicBookDlqName);
        assertThat(binding.getExchange()).isEqualTo(comicBookDlxName);
        assertThat(binding.getRoutingKey()).isEqualTo(comicBookQueueName);
    }
}