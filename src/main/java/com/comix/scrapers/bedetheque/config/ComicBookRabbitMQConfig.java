package com.comix.scrapers.bedetheque.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ComicBookRabbitMQConfig {

    @Value("${amqp.queue.comic-book.name}")
    private String comicBookQueueName;

    // --- Définition des noms pour une meilleure lisibilité ---
    public static final class ComicBookQueueConfig {
        public static final String EXCHANGE = "comix.comic-book.exchange";
        public static final String DLQ = "comix.comic-book.synchronize.dlq";
        public static final String DLX = "comix.comic-book.synchronize.dlx";
    }

    public static final class ComicBookRetryQueueConfig {
        public static final String QUEUE = "comix.comic-book.synchronize.retry.dlq";
        public static final String EXCHANGE = "comix.comic-book.synchronize.retry.dlx";
        public static final int TTL = 10000; // 10 secondes
    }

    // --- Échanges ---
    @Bean
    DirectExchange comicBookExchange() {
        return new DirectExchange(ComicBookQueueConfig.EXCHANGE);
    }

    @Bean
    DirectExchange comicBookDeadLetterExchange() {
        return new DirectExchange(ComicBookQueueConfig.DLX);
    }

    @Bean
    DirectExchange comicBookRetryDeadLetterExchange() {
        return new DirectExchange(ComicBookRetryQueueConfig.EXCHANGE);
    }

    // --- File d'attente principale ---
    @Bean
    Queue comicBookQueue() {
        return QueueBuilder.durable(comicBookQueueName)
                .withArgument("x-dead-letter-exchange", ComicBookRetryQueueConfig.EXCHANGE) // En cas d'échec, envoyer vers l'échange de retry
                .build();
    }

    // --- File d'attente de re-tentative (avec TTL) ---
    @Bean
    Queue comicBookRetryQueue() {
        return QueueBuilder.durable(ComicBookRetryQueueConfig.QUEUE)
                .withArgument("x-dead-letter-exchange", ComicBookQueueConfig.EXCHANGE) // Après TTL, renvoyer vers l'échange principal
                .withArgument("x-message-ttl", ComicBookRetryQueueConfig.TTL)
                .build();
    }

    // --- File d'attente finale pour les échecs (DLQ) ---
    @Bean
    Queue comicBookDeadLetterQueue() {
        return new Queue(ComicBookQueueConfig.DLQ);
    }

    // --- Liaisons (Bindings) ---
    @Bean
    Binding comicBookBinding(Queue comicBookQueue, DirectExchange comicBookExchange) {
        return BindingBuilder.bind(comicBookQueue).to(comicBookExchange).with(comicBookQueueName);
    }

    @Bean
    Binding comicBookRetryBinding(Queue comicBookRetryQueue, DirectExchange comicBookRetryDeadLetterExchange) {
        return BindingBuilder.bind(comicBookRetryQueue).to(comicBookRetryDeadLetterExchange).with(comicBookQueueName);
    }

    @Bean
    Binding comicBookDlqBinding(Queue comicBookDeadLetterQueue, DirectExchange comicBookDeadLetterExchange) {
        return BindingBuilder.bind(comicBookDeadLetterQueue).to(comicBookDeadLetterExchange).with(comicBookQueueName);
    }
}