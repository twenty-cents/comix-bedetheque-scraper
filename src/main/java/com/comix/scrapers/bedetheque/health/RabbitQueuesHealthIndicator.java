package com.comix.scrapers.bedetheque.health;

import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * HealthIndicator personnalisé qui vérifie la présence des files d'attente RabbitMQ nécessaires à l'application.
 */
@Component
public class RabbitQueuesHealthIndicator implements HealthIndicator {

    private final RabbitAdmin rabbitAdmin;
    private final List<String> requiredQueues;

    public RabbitQueuesHealthIndicator(
            RabbitAdmin rabbitAdmin,
            @Value("${amqp.queue.author.name}") String authorQueue,
            @Value("${amqp.queue.serie.name}") String serieQueue,
            @Value("${amqp.queue.comicBook.name}") String comicBookQueue
    ) {
        this.rabbitAdmin = rabbitAdmin;
        this.requiredQueues = List.of(authorQueue, serieQueue, comicBookQueue);
    }

    @Override
    public Health health() {
        List<String> missingQueues = new ArrayList<>();

        for (String queueName : requiredQueues) {
            // getQueueProperties retourne null si la file n'existe pas.
            Properties queueProperties = rabbitAdmin.getQueueProperties(queueName);
            if (queueProperties == null) {
                missingQueues.add(queueName);
            }
        }

        if (!missingQueues.isEmpty()) {
            return Health.down()
                    .withDetail("reason", "Les files d'attente RabbitMQ suivantes sont manquantes")
                    .withDetail("missing_queues", missingQueues)
                    .build();
        }

        return Health.up()
                .withDetail("queues", requiredQueues)
                .build();
    }
}