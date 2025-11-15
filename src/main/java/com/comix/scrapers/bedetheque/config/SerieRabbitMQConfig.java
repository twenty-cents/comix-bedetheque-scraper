package com.comix.scrapers.bedetheque.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SerieRabbitMQConfig {

    @Value("${amqp.queue.serie.name}")
    private String serieQueueName;

    // --- Définition des noms pour une meilleure lisibilité ---
    public static final class SerieQueueConfig {
        public static final String EXCHANGE = "comix.serie.exchange";
        public static final String DLQ = "comix.serie.synchronize.dlq";
        public static final String DLX = "comix.serie.synchronize.dlx";
    }

    public static final class SerieRetryQueueConfig {
        public static final String QUEUE = "comix.serie.synchronize.retry.dlq";
        public static final String EXCHANGE = "comix.serie.synchronize.retry.dlx";
        public static final int TTL = 10000; // 10 secondes
    }

    // --- Échanges ---
    @Bean
    DirectExchange serieExchange() {
        return new DirectExchange(SerieQueueConfig.EXCHANGE);
    }

    @Bean
    DirectExchange serieDeadLetterExchange() {
        return new DirectExchange(SerieQueueConfig.DLX);
    }

    @Bean
    DirectExchange serieRetryDeadLetterExchange() {
        return new DirectExchange(SerieRetryQueueConfig.EXCHANGE);
    }

    // --- File d'attente principale ---
    @Bean
    Queue serieQueue() {
        return QueueBuilder.durable(serieQueueName)
                .withArgument("x-dead-letter-exchange", SerieRetryQueueConfig.EXCHANGE) // En cas d'échec, envoyer vers l'échange de retry
                .build();
    }

    // --- File d'attente de re-tentative (avec TTL) ---
    @Bean
    Queue serieRetryQueue() {
        return QueueBuilder.durable(SerieRetryQueueConfig.QUEUE)
                .withArgument("x-dead-letter-exchange", SerieQueueConfig.EXCHANGE) // Après TTL, renvoyer vers l'échange principal
                .withArgument("x-message-ttl", SerieRetryQueueConfig.TTL)
                .build();
    }

    // --- File d'attente finale pour les échecs (DLQ) ---
    @Bean
    Queue serieDeadLetterQueue() {
        return new Queue(SerieQueueConfig.DLQ);
    }

    // --- Liaisons (Bindings) ---
    @Bean
    Binding serieBinding(Queue serieQueue, DirectExchange serieExchange) {
        return BindingBuilder.bind(serieQueue).to(serieExchange).with(serieQueueName);
    }

    @Bean
    Binding serieRetryBinding(Queue serieRetryQueue, DirectExchange serieRetryDeadLetterExchange) {
        return BindingBuilder.bind(serieRetryQueue).to(serieRetryDeadLetterExchange).with(serieQueueName);
    }

    @Bean
    Binding serieDlqBinding(Queue serieDeadLetterQueue, DirectExchange serieDeadLetterExchange) {
        return BindingBuilder.bind(serieDeadLetterQueue).to(serieDeadLetterExchange).with(serieQueueName);
    }
}