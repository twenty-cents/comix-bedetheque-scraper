package com.comix.scrapers.bedetheque.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration pour les beans spécifiques à RabbitMQ qui ne sont pas auto-configurés.
 */
@Configuration
public class RabbitMQConfiguration {

    /**
     * Crée un bean RabbitAdmin, nécessaire pour les opérations d'administration sur le broker RabbitMQ,
     * comme la vérification de l'existence des files d'attente dans notre HealthIndicator.
     * @param connectionFactory La ConnectionFactory auto-configurée par Spring Boot.
     * @return Une instance de RabbitAdmin.
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}