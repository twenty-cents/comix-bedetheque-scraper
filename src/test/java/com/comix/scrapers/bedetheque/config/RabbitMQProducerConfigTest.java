package com.comix.scrapers.bedetheque.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(properties = {
    "producer.retry.max-attempts=5",
    "producer.retry.initial-interval=100",
    "producer.retry.max-interval=5000",
    "producer.retry.multiplier=2.5"
})
class RabbitMQProducerConfigTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RetryTemplate retryTemplate;

    @Test
    @DisplayName("RabbitTemplate should be configured with JSON converter and RetryTemplate")
    void rabbitTemplate_shouldBeCorrectlyConfigured() {
        assertThat(rabbitTemplate).isNotNull();
        assertThat(rabbitTemplate.getMessageConverter()).isInstanceOf(Jackson2JsonMessageConverter.class);

        // Utilisation de la réflexion pour accéder au champ privé retryTemplate
        Field retryTemplateField = ReflectionUtils.findField(RabbitTemplate.class, "retryTemplate");
        assertThat(retryTemplateField).isNotNull();
        retryTemplateField.setAccessible(true);
        Object actualRetryTemplate = ReflectionUtils.getField(retryTemplateField, rabbitTemplate);

        assertThat(actualRetryTemplate).isSameAs(retryTemplate);
    }

    @Test
    @DisplayName("RetryTemplate should retry the configured number of times on failure")
    void retryTemplate_shouldRetryOnFailure() {
        assertThat(retryTemplate).isNotNull();

        AtomicInteger attempts = new AtomicInteger(0);
        RuntimeException testException = new RuntimeException("Simulated failure");

        // On exécute une action qui échoue à chaque fois
        assertThatThrownBy(() -> retryTemplate.execute((RetryCallback<Void, RuntimeException>) context -> {
            attempts.incrementAndGet();
            throw testException;
        })).isSameAs(testException);

        // On vérifie que le nombre de tentatives correspond à la configuration (max-attempts=5)
        assertThat(attempts.get()).isEqualTo(5);
    }
}