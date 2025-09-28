package com.comix.scrapers.bedetheque.service;

import com.comix.scrapers.bedetheque.entity.OutboxMessage;
import com.comix.scrapers.bedetheque.repository.OutboxMessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxMessagePublisherTest {

    @Mock
    private OutboxMessageRepository outboxRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OutboxMessagePublisher outboxMessagePublisher;

    @Test
    @DisplayName("Should do nothing when no pending messages are found")
    void publishPendingMessages_whenNoPendingMessages_shouldDoNothing() {
        // Given
        when(outboxRepository.findByStatus(OutboxMessage.Status.PENDING)).thenReturn(Collections.emptyList());

        // When
        outboxMessagePublisher.publishPendingMessages();

        // Then
        verify(outboxRepository).findByStatus(OutboxMessage.Status.PENDING);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("Should publish pending messages and mark them as SENT")
    void publishPendingMessages_whenPendingMessagesExist_shouldPublishAndMarkAsSent() {
        // Given
        OutboxMessage message1 = new OutboxMessage();
        message1.setId(1L);
        message1.setExchange("exchange1");
        message1.setRoutingKey("key1");
        message1.setPayload("payload1");

        OutboxMessage message2 = new OutboxMessage();
        message2.setId(2L);
        message2.setExchange("exchange2");
        message2.setRoutingKey("key2");
        message2.setPayload("payload2");

        List<OutboxMessage> pendingMessages = List.of(message1, message2);
        when(outboxRepository.findByStatus(OutboxMessage.Status.PENDING)).thenReturn(pendingMessages);

        // When
        outboxMessagePublisher.publishPendingMessages();

        // Then
        verify(rabbitTemplate).convertAndSend("exchange1", "key1", "payload1");
        verify(rabbitTemplate).convertAndSend("exchange2", "key2", "payload2");

        ArgumentCaptor<OutboxMessage> messageCaptor = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(outboxRepository, times(2)).save(messageCaptor.capture());

        List<OutboxMessage> savedMessages = messageCaptor.getAllValues();
        assertThat(savedMessages).extracting(OutboxMessage::getStatus).containsOnly(OutboxMessage.Status.SENT);
    }

    @Test
    @DisplayName("Should not update message status if publishing fails")
    void publishPendingMessages_whenPublishingFails_shouldNotUpdateStatus() {
        // Given
        OutboxMessage message = new OutboxMessage();
        message.setId(1L);
        message.setExchange("test-exchange");
        message.setRoutingKey("test-key");
        message.setPayload("test-payload");

        when(outboxRepository.findByStatus(OutboxMessage.Status.PENDING)).thenReturn(List.of(message));
        doThrow(new AmqpException("Connection failed")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString());

        // When
        outboxMessagePublisher.publishPendingMessages();

        // Then
        // Verify we attempted to send
        verify(rabbitTemplate).convertAndSend("test-exchange", "test-key", "test-payload");
        // Verify we did NOT save the message, as an exception was caught
        verify(outboxRepository, never()).save(any(OutboxMessage.class));
    }
}