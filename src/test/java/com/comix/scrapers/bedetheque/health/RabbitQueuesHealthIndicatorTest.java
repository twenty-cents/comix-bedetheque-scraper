package com.comix.scrapers.bedetheque.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RabbitQueuesHealthIndicatorTest {

    @Mock
    private RabbitAdmin rabbitAdmin;

    private RabbitQueuesHealthIndicator healthIndicator;

    private final String authorQueue = "author.queue";
    private final String serieQueue = "serie.queue";
    private final String comicBookQueue = "comicbook.queue";

    @BeforeEach
    void setUp() {
        // On instancie manuellement l'indicateur avant chaque test
        healthIndicator = new RabbitQueuesHealthIndicator(rabbitAdmin, authorQueue, serieQueue, comicBookQueue);
    }

    @Test
    @DisplayName("Should return UP when all required queues exist")
    void health_shouldReturnUp_whenAllQueuesExist() {
        // Given: Le RabbitAdmin retourne des propriétés pour chaque file (simulant leur existence)
        Properties dummyProperties = new Properties();
        when(rabbitAdmin.getQueueProperties(authorQueue)).thenReturn(dummyProperties);
        when(rabbitAdmin.getQueueProperties(serieQueue)).thenReturn(dummyProperties);
        when(rabbitAdmin.getQueueProperties(comicBookQueue)).thenReturn(dummyProperties);

        // When
        Health health = healthIndicator.health();

        // Then
        assertEquals(Status.UP, health.getStatus());
        assertEquals(List.of(authorQueue, serieQueue, comicBookQueue), health.getDetails().get("queues"));
    }

    @Test
    @DisplayName("Should return DOWN when one queue is missing")
    void health_shouldReturnDown_whenOneQueueIsMissing() {
        // Given: Le RabbitAdmin retourne null pour une des files
        Properties dummyProperties = new Properties();
        when(rabbitAdmin.getQueueProperties(authorQueue)).thenReturn(dummyProperties);
        when(rabbitAdmin.getQueueProperties(serieQueue)).thenReturn(null); // La file manquante
        when(rabbitAdmin.getQueueProperties(comicBookQueue)).thenReturn(dummyProperties);

        // When
        Health health = healthIndicator.health();

        // Then
        assertEquals(Status.DOWN, health.getStatus());
        List<String> missingQueues = (List<String>) health.getDetails().get("missing_queues");
        assertEquals(1, missingQueues.size());
        assertTrue(missingQueues.contains(serieQueue));
    }

    @Test
    @DisplayName("Should return DOWN when all queues are missing")
    void health_shouldReturnDown_whenAllQueuesAreMissing() {
        // Given: Le RabbitAdmin retourne null pour toutes les files
        when(rabbitAdmin.getQueueProperties(authorQueue)).thenReturn(null);
        when(rabbitAdmin.getQueueProperties(serieQueue)).thenReturn(null);
        when(rabbitAdmin.getQueueProperties(comicBookQueue)).thenReturn(null);

        // When
        Health health = healthIndicator.health();

        // Then
        assertEquals(Status.DOWN, health.getStatus());
        List<String> missingQueues = (List<String>) health.getDetails().get("missing_queues");
        assertEquals(3, missingQueues.size());
        assertTrue(missingQueues.containsAll(List.of(authorQueue, serieQueue, comicBookQueue)));
    }
}