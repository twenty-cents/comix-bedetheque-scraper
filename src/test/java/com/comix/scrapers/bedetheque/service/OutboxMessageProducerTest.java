package com.comix.scrapers.bedetheque.service;

import com.comix.scrapers.bedetheque.entity.OutboxMessage;
import com.comix.scrapers.bedetheque.repository.OutboxMessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxMessageProducerTest {

    @Mock
    private OutboxMessageRepository outboxRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxMessageProducer outboxMessageProducer;

    @Test
    @DisplayName("Should save message to outbox when publisher is enabled")
    void saveToOutbox_whenEnabled_shouldSaveMessage() throws JsonProcessingException {
        // Given
        ReflectionTestUtils.setField(outboxMessageProducer, "outboxPublisherEnabled", true);
        String exchange = "test-exchange";
        String routingKey = "test-key";
        DummyPayload payload = new DummyPayload("test-data");
        String jsonPayload = "{\"data\":\"test-data\"}";

        when(objectMapper.writeValueAsString(payload)).thenReturn(jsonPayload);

        // When
        outboxMessageProducer.saveToOutbox(exchange, routingKey, payload);

        // Then
        ArgumentCaptor<OutboxMessage> outboxMessageCaptor = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(outboxRepository).save(outboxMessageCaptor.capture());

        OutboxMessage capturedMessage = outboxMessageCaptor.getValue();
        assertThat(capturedMessage.getExchange()).isEqualTo(exchange);
        assertThat(capturedMessage.getRoutingKey()).isEqualTo(routingKey);
        assertThat(capturedMessage.getPayload()).isEqualTo(jsonPayload);
    }

    @Test
    @DisplayName("Should do nothing when publisher is disabled")
    void saveToOutbox_whenDisabled_shouldDoNothing() {
        // Given
        ReflectionTestUtils.setField(outboxMessageProducer, "outboxPublisherEnabled", false);

        // When
        outboxMessageProducer.saveToOutbox("any-exchange", "any-key", new DummyPayload("any-data"));

        // Then
        verifyNoInteractions(objectMapper, outboxRepository);
    }

    @Test
    @DisplayName("Should log error and not save when serialization fails")
    void saveToOutbox_whenSerializationFails_shouldLogError() throws JsonProcessingException {
        // Given
        ReflectionTestUtils.setField(outboxMessageProducer, "outboxPublisherEnabled", true);
        DummyPayload payload = new DummyPayload("test-data");

        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Test Exception") {});

        // When
        outboxMessageProducer.saveToOutbox("any-exchange", "any-key", payload);

        // Then
        verify(outboxRepository, never()).save(any());
    }

    @Data @AllArgsConstructor
    private static class DummyPayload {
        private String data;
    }
}