package com.comix.scrapers.bedetheque.repository;

import com.comix.scrapers.bedetheque.config.AbstractIntegrationTest;
import com.comix.scrapers.bedetheque.entity.OutboxMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OutboxMessageRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OutboxMessageRepository outboxMessageRepository;

    @Test
    @DisplayName("Should save and retrieve an OutboxMessage with default values")
    void shouldSaveAndRetrieveOutboxMessage() {
        // Given
        OutboxMessage newMessage = new OutboxMessage();
        newMessage.setExchange("test-exchange");
        newMessage.setRoutingKey("test-key");
        newMessage.setPayload("{\"message\":\"hello\"}");

        // When
        OutboxMessage savedMessage = entityManager.persistFlushFind(newMessage);

        // Then
        assertThat(savedMessage).isNotNull();
        assertThat(savedMessage.getId()).isNotNull().isGreaterThan(0L);
        assertThat(savedMessage.getExchange()).isEqualTo("test-exchange");
        assertThat(savedMessage.getStatus()).isEqualTo(OutboxMessage.Status.PENDING);
        assertThat(savedMessage.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("findByStatus should return only messages with the specified status")
    void findByStatus_shouldReturnOnlyPendingMessages() {
        // Given
        OutboxMessage pendingMessage = new OutboxMessage();
        pendingMessage.setStatus(OutboxMessage.Status.PENDING);
        pendingMessage.setPayload("{}");
        entityManager.persist(pendingMessage);

        OutboxMessage sentMessage = new OutboxMessage();
        sentMessage.setStatus(OutboxMessage.Status.SENT);
        sentMessage.setPayload("{}");
        entityManager.persist(sentMessage);

        // When
        List<OutboxMessage> foundMessages = outboxMessageRepository.findByStatus(OutboxMessage.Status.PENDING);

        // Then
        assertThat(foundMessages).hasSize(1).containsExactly(pendingMessage);
    }
}