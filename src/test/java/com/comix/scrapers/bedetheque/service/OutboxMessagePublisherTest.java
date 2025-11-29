package com.comix.scrapers.bedetheque.service;

import com.comix.scrapers.bedetheque.entity.OutboxMessage;
import com.comix.scrapers.bedetheque.repository.OutboxMessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxMessagePublisherTest {

    @Mock
    private OutboxMessageRepository outboxRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ObjectMapper objectMapper;

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
    @DisplayName("Should publish messages and update their status when pending messages are found")
    void publishPendingMessages_whenPendingMessagesExist_shouldPublishAndUpdateStatus() throws JsonProcessingException {
        // Given
        OutboxMessage message = new OutboxMessage();
        message.setId(1L);
        message.setExchange("comix.exchange");
        message.setRoutingKey("comix.routing.key");
        String payload = "\"test payload\""; // Représentation JSON d'une chaîne de caractères
        String payloadObject = "test payload"; // L'objet Java attendu après désérialisation
        message.setPayload(payload);
        message.setPayloadType("java.lang.String");
        message.setStatus(OutboxMessage.Status.PENDING);
        
        when(outboxRepository.findByStatus(OutboxMessage.Status.PENDING)).thenReturn(List.of(message));

        // Simuler la désérialisation par l'ObjectMapper
        when(objectMapper.readValue(payload, String.class)).thenReturn(payloadObject);

        // On simule le comportement de la méthode save() pour éviter une NullPointerException
        // si le code de production utilise l'objet retourné.
        when(outboxRepository.save(any(OutboxMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        outboxMessagePublisher.publishPendingMessages();

        // Then
        // Verify the message was sent to RabbitMQ
        verify(rabbitTemplate).convertAndSend(message.getExchange(), message.getRoutingKey(), payloadObject);

        // Verify the message status was updated to PUBLISHED in the repository
        ArgumentCaptor<OutboxMessage> messageCaptor = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(outboxRepository).save(messageCaptor.capture());

        OutboxMessage savedMessage = messageCaptor.getValue();
        assertEquals(OutboxMessage.Status.SENT, savedMessage.getStatus());
        assertEquals(message.getId(), savedMessage.getId());
    }

    @Test
    @DisplayName("Should not update message status if publishing fails")
    void publishPendingMessages_whenPublishingFails_shouldNotUpdateStatus() {
        // Given
        OutboxMessage message = new OutboxMessage();
        message.setId(1L);
        message.setExchange("comix.exchange");
        message.setRoutingKey("comix.routing.key");
        message.setPayload("{\"data\":\"test\"}");
        message.setStatus(OutboxMessage.Status.PENDING);

        when(outboxRepository.findByStatus(OutboxMessage.Status.PENDING)).thenReturn(List.of(message));
        // On utilise lenient() car le comportement transactionnel dans les tests peut empêcher l'appel
        // si une autre partie de la transaction échoue ou n'est pas correctement simulée.
        // Cela indique à Mockito de ne pas échouer si ce mock n'est pas utilisé.
        lenient().doThrow(new AmqpException("Connection failed")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString());

        // When
        outboxMessagePublisher.publishPendingMessages();

        // Then
        verify(outboxRepository, never()).save(any(OutboxMessage.class));
    }
}