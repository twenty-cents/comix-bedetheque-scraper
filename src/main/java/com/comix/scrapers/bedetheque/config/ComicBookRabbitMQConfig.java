package com.comix.scrapers.bedetheque.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ComicBookRabbitMQConfig {

    @Value("${amqp.queue.comic-book.name}")
    private String comicBookQueue;

    @Value("${amqp.exchange.comic-book.name}")
    private String comicBookExchange;

    @Value("${amqp.queue.comic-book.dlq.name}")
    private String comicBookDlq;

    @Value("${amqp.exchange.comic-book.dlx.name}")
    private String comicBookDlx;

    @Value("${amqp.queue.comic-book.retry.name}")
    private String comicBookRetryQueue;

    @Value("${amqp.exchange.comic-book.retry.name}")
    private String comicBookRetryExchange;

    @Value("${amqp.queue.comic-book.retry.ttl}")
    private int comicBookRetryTtl;

    // --- Échanges ---
    @Bean
    DirectExchange comicBookExchange() {
        return new DirectExchange(comicBookExchange);
    }

    @Bean
    DirectExchange comicBookDeadLetterExchange() {
        return new DirectExchange(comicBookDlx);
    }

    @Bean
    DirectExchange comicBookRetryDeadLetterExchange() {
        return new DirectExchange(comicBookRetryExchange);
    }

    // --- File d'attente principale ---
    @Bean
    Queue comicBookQueue() {
        return QueueBuilder.durable(comicBookQueue)
                .withArgument("x-dead-letter-exchange", comicBookRetryExchange) // En cas d'échec, envoyer vers l'échange de retry
                .build();
    }

    // --- File d'attente de re-tentative (avec TTL) ---
    @Bean
    Queue comicBookRetryQueue() {
        return QueueBuilder.durable(comicBookRetryQueue)
                .withArgument("x-dead-letter-exchange", comicBookExchange) // Après TTL, renvoyer vers l'échange principal
                .withArgument("x-message-ttl", comicBookRetryTtl)
                .build();
    }

    // --- File d'attente finale pour les échecs (DLQ) ---
    @Bean
    Queue comicBookDeadLetterQueue() {
        return new Queue(comicBookDlq);
    }

    // --- Liaisons (Bindings) ---
    @Bean
    Binding comicBookBinding(Queue comicBookQueue, DirectExchange comicBookExchange) {
        return BindingBuilder.bind(comicBookQueue).to(comicBookExchange).with(this.comicBookQueue);
    }

    @Bean
    Binding comicBookRetryBinding(Queue comicBookRetryQueue, DirectExchange comicBookRetryDeadLetterExchange) {
        return BindingBuilder.bind(comicBookRetryQueue).to(comicBookRetryDeadLetterExchange).with(comicBookQueue);
    }

    @Bean
    Binding comicBookDlqBinding(Queue comicBookDeadLetterQueue, DirectExchange comicBookDeadLetterExchange) {
        return BindingBuilder.bind(comicBookDeadLetterQueue).to(comicBookDeadLetterExchange).with(comicBookQueue);
    }
}