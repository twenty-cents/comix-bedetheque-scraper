package com.comix.scrapers.bedetheque.service;

import com.comix.scrapers.bedetheque.entity.OutboxMessage;
import com.comix.scrapers.bedetheque.repository.OutboxMessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Collections;

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
}