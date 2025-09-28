package com.comix.scrapers.bedetheque.service;

import com.comix.scrapers.bedetheque.entity.OutboxMessage;
import com.comix.scrapers.bedetheque.repository.OutboxMessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service responsible for creating and saving messages to the outbox table for later publishing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxMessageProducer {

    private final OutboxMessageRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Value("${outbox.publisher.enabled:true}")
    private boolean outboxPublisherEnabled;

    public void saveToOutbox(String exchange, String routingKey, Object payload) {
        if (!outboxPublisherEnabled) {
            return;
        }

        try {
            log.info("Saving message to outbox for publishing. Exchange: '{}', RoutingKey: '{}'", exchange, routingKey);
            OutboxMessage outboxMessage = new OutboxMessage();
            outboxMessage.setExchange(exchange);
            outboxMessage.setRoutingKey(routingKey);
            outboxMessage.setPayload(objectMapper.writeValueAsString(payload));
            outboxRepository.save(outboxMessage);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize payload for outbox. Message will be lost! Payload class: {}", payload.getClass().getName(), e);
        }
    }
}