package com.comix.scrapers.bedetheque.service;

import com.comix.scrapers.bedetheque.entity.OutboxMessage;
import com.comix.scrapers.bedetheque.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxMessagePublisher {

    private final OutboxMessageRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelayString = "${outbox.publisher.fixed-delay}") // Exécute toutes les 10 secondes par défaut
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
                rabbitTemplate.convertAndSend(message.getExchange(), message.getRoutingKey(), message.getPayload());
                message.setStatus(OutboxMessage.Status.SENT);
                outboxRepository.save(message);
            } catch (Exception e) {
                log.warn("Failed to publish message ID {}. It will be retried later.", message.getId(), e);
                // La transaction sera rollback, le statut restera PENDING
            }
        }
    }
}