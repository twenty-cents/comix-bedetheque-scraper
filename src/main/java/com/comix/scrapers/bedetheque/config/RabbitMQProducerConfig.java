package com.comix.scrapers.bedetheque.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Slf4j
@Configuration
public class RabbitMQProducerConfig {

    @Value("${producer.retry.max-attempts}")
    private int maxAttempts;

    @Value("${producer.retry.initial-interval}")
    private long initialInterval;

    @Value("${producer.retry.max-interval}")
    private long maxInterval;

    @Value("${producer.retry.multiplier}")
    private double multiplier;

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        // S'assurer que les objets sont bien sérialisés en JSON
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());

        // Appliquer notre politique de re-tentative
        rabbitTemplate.setRetryTemplate(retryTemplate());

        return rabbitTemplate;
    }

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Définir une politique de backoff exponentiel
        // 1ère tentative après 500ms, 2ème après 1s, 3ème après 2s...
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialInterval);
        backOffPolicy.setMultiplier(multiplier);
        backOffPolicy.setMaxInterval(maxInterval);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // Définir la politique de re-tentative
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(maxAttempts);
        retryTemplate.setRetryPolicy(retryPolicy);

        // Ajouter un listener pour logger les tentatives
        retryTemplate.setListeners(new RetryListener[]{
            new RetryListener() {
                @Override
                public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                    // Ce log est émis à chaque tentative échouée
                    log.warn("Attempt {} to send message to RabbitMQ failed. Retrying...", context.getRetryCount(), throwable);
                }

                @Override
                public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                    // Ce log est émis uniquement si toutes les tentatives ont échoué
                    if (throwable != null) {
                        log.error("Failed to send message to RabbitMQ after {} attempts.", context.getRetryCount(), throwable);
                    }
                }
            }
        });

        return retryTemplate;
    }
}