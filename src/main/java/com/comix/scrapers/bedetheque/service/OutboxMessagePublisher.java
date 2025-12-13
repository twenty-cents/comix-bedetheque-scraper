package com.comix.scrapers.bedetheque.service;

import com.comix.scrapers.bedetheque.entity.OutboxMessage;
import com.comix.scrapers.bedetheque.repository.OutboxMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "outbox.publisher.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class OutboxMessagePublisher {

    private final OutboxMessageRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${outbox.publisher.fixed-delay}")
    @SchedulerLock(name = "publishPendingOutboxMessages",
            lockAtLeastFor = "${outbox.publisher.lock-at-least-for}",
            lockAtMostFor = "${outbox.publisher.lock-at-most-for}")
    @Transactional
    public void publishPendingMessages() {
        List<OutboxMessage> messages = outboxRepository.findByStatus(OutboxMessage.Status.PENDING);
        if (messages.isEmpty()) {
            return;
        }

        log.info("Found {} pending messages in outbox. Attempting to publish...", messages.size());

        for (OutboxMessage message : messages) {
            try {
                // Re-crée l'objet Java à partir du JSON stocké et du type stocké
                Class<?> payloadType = Class.forName(message.getPayloadType());
                Object payloadObject = objectMapper.readValue(message.getPayload(), payloadType);

                // Envoie l'objet Java, pas la chaîne de caractères JSON
                rabbitTemplate.convertAndSend(message.getExchange(), message.getRoutingKey(), payloadObject);

                message.setStatus(OutboxMessage.Status.SENT);
                outboxRepository.save(message);
                log.info("Message ID {} published successfully. Exchange: '{}', RoutingKey: '{}', Content: '{}'",
                        message.getId(), message.getExchange(), message.getRoutingKey(), message.getContent());
            } catch (Exception e) {
                log.warn("Failed to publish message ID {}. It will be retried later. Content: '{}'", message.getId(), message.getContent(), e);
                // La transaction sera rollback, le statut restera PENDING
            }
        }
    }
}