package com.comix.scrapers.bedetheque.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthorRabbitMQConfig {

    @Value("${amqp.queue.author.name}")
    private String authorQueueName;

    // --- Définition des noms pour une meilleure lisibilité ---
    public static final class AuthorQueueConfig {
        public static final String EXCHANGE = "comix.author.exchange";
        public static final String DLQ = "comix.author.synchronize.dlq";
        public static final String DLX = "comix.author.synchronize.dlx";
    }

    public static final class AuthorRetryQueueConfig {
        public static final String QUEUE = "comix.author.synchronize.retry.dlq";
        public static final String EXCHANGE = "comix.author.synchronize.retry.dlx";
        public static final int TTL = 10000; // 10 secondes
    }

    // --- Échanges ---
    @Bean
    DirectExchange authorExchange() {
        return new DirectExchange(AuthorQueueConfig.EXCHANGE);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(AuthorQueueConfig.DLX);
    }

    @Bean
    DirectExchange retryDeadLetterExchange() {
        return new DirectExchange(AuthorRetryQueueConfig.EXCHANGE);
    }

    // --- File d'attente principale ---
    @Bean
    Queue authorQueue() {
        return QueueBuilder.durable(authorQueueName)
                .withArgument("x-dead-letter-exchange", AuthorRetryQueueConfig.EXCHANGE) // En cas d'échec, envoyer vers l'échange de retry
                .build();
    }

    // --- File d'attente de re-tentative (avec TTL) ---
    @Bean
    Queue retryQueue() {
        return QueueBuilder.durable(AuthorRetryQueueConfig.QUEUE)
                .withArgument("x-dead-letter-exchange", AuthorQueueConfig.EXCHANGE) // Après TTL, renvoyer vers l'échange principal
                .withArgument("x-message-ttl", AuthorRetryQueueConfig.TTL)
                .build();
    }

    // --- File d'attente finale pour les échecs (DLQ) ---
    @Bean
    Queue deadLetterQueue() {
        return new Queue(AuthorQueueConfig.DLQ);
    }

    // --- Liaisons (Bindings) ---
    @Bean
    Binding authorBinding(Queue authorQueue, DirectExchange authorExchange) {
        return BindingBuilder.bind(authorQueue).to(authorExchange).with(authorQueueName);
    }

    @Bean
    Binding retryBinding(Queue retryQueue, DirectExchange retryDeadLetterExchange) {
        // La clé de routage doit être la même que celle utilisée pour la file principale
        return BindingBuilder.bind(retryQueue).to(retryDeadLetterExchange).with(authorQueueName);
    }

    @Bean
    Binding dlqBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        // La clé de routage doit être la même que celle utilisée pour la file principale
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(authorQueueName);
    }
}