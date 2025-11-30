package com.comix.scrapers.bedetheque.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthorRabbitMQConfig {

    @Value("${amqp.queue.author.name}")
    private String authorQueue;

    @Value("${amqp.exchange.author.name}")
    private String authorExchange;

    @Value("${amqp.queue.author.dlq.name}")
    private String authorDlq;

    @Value("${amqp.exchange.author.dlx.name}")
    private String authorDlx;

    @Value("${amqp.queue.author.retry.name}")
    private String authorRetryQueue;

    @Value("${amqp.exchange.author.retry.name}")
    private String authorRetryExchange;

    @Value("${amqp.queue.author.retry.ttl}")
    private int authorRetryTtl;

    // --- Échanges ---
    @Bean
    DirectExchange authorExchange() {
        return new DirectExchange(authorExchange);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(authorDlx);
    }

    @Bean
    DirectExchange retryDeadLetterExchange() {
        return new DirectExchange(authorRetryExchange);
    }

    // --- File d'attente principale ---
    @Bean
    Queue authorQueue() {
        return QueueBuilder.durable(authorQueue)
                .withArgument("x-dead-letter-exchange", authorRetryExchange) // En cas d'échec, envoyer vers l'échange de retry
                .build();
    }

    // --- File d'attente de re-tentative (avec TTL) ---
    @Bean
    Queue retryQueue() {
        return QueueBuilder.durable(authorRetryQueue)
                .withArgument("x-dead-letter-exchange", authorExchange) // Après TTL, renvoyer vers l'échange principal
                .withArgument("x-message-ttl", authorRetryTtl)
                .build();
    }

    // --- File d'attente finale pour les échecs (DLQ) ---
    @Bean
    Queue deadLetterQueue() {
        return new Queue(authorDlq);
    }

    // --- Liaisons (Bindings) ---
    @Bean
    Binding authorBinding(Queue authorQueue, DirectExchange authorExchange) {
        return BindingBuilder.bind(authorQueue).to(authorExchange).with(this.authorQueue);
    }

    @Bean
    Binding retryBinding(Queue retryQueue, DirectExchange retryDeadLetterExchange) {
        // La clé de routage doit être la même que celle utilisée pour la file principale
        return BindingBuilder.bind(retryQueue).to(retryDeadLetterExchange).with(authorQueue);
    }

    @Bean
    Binding dlqBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        // La clé de routage doit être la même que celle utilisée pour la file principale
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(authorQueue);
    }
}