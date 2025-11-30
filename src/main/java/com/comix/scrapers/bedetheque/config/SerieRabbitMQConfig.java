package com.comix.scrapers.bedetheque.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SerieRabbitMQConfig {

    @Value("${amqp.queue.serie.name}")
    private String serieQueue;

    @Value("${amqp.exchange.serie.name}")
    private String serieExchange;

    @Value("${amqp.queue.serie.dlq.name}")
    private String serieDlq;

    @Value("${amqp.exchange.serie.dlx.name}")
    private String serieDlx;

    @Value("${amqp.queue.serie.retry.name}")
    private String serieRetryQueue;

    @Value("${amqp.exchange.serie.retry.name}")
    private String serieRetryExchange;

    @Value("${amqp.queue.serie.retry.ttl}")
    private int serieRetryTtl;

    // --- Échanges ---
    @Bean
    DirectExchange serieExchange() {
        return new DirectExchange(serieExchange);
    }

    @Bean
    DirectExchange serieDeadLetterExchange() {
        return new DirectExchange(serieDlx);
    }

    @Bean
    DirectExchange serieRetryDeadLetterExchange() {
        return new DirectExchange(serieRetryExchange);
    }

    // --- File d'attente principale ---
    @Bean
    Queue serieQueue() {
        return QueueBuilder.durable(serieQueue)
                .withArgument("x-dead-letter-exchange", serieRetryExchange) // En cas d'échec, envoyer vers l'échange de retry
                .build();
    }

    // --- File d'attente de re-tentative (avec TTL) ---
    @Bean
    Queue serieRetryQueue() {
        return QueueBuilder.durable(serieRetryQueue)
                .withArgument("x-dead-letter-exchange", serieExchange) // Après TTL, renvoyer vers l'échange principal
                .withArgument("x-message-ttl", serieRetryTtl)
                .build();
    }

    // --- File d'attente finale pour les échecs (DLQ) ---
    @Bean
    Queue serieDeadLetterQueue() {
        return new Queue(serieDlq);
    }

    // --- Liaisons (Bindings) ---
    @Bean
    Binding serieBinding(Queue serieQueue, DirectExchange serieExchange) {
        return BindingBuilder.bind(serieQueue).to(serieExchange).with(this.serieQueue);
    }

    @Bean
    Binding serieRetryBinding(Queue serieRetryQueue, DirectExchange serieRetryDeadLetterExchange) {
        return BindingBuilder.bind(serieRetryQueue).to(serieRetryDeadLetterExchange).with(serieQueue);
    }

    @Bean
    Binding serieDlqBinding(Queue serieDeadLetterQueue, DirectExchange serieDeadLetterExchange) {
        return BindingBuilder.bind(serieDeadLetterQueue).to(serieDeadLetterExchange).with(serieQueue);
    }
}