package com.comix.scrapers.bedetheque.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.NotBlank;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MessageSourceConfig}.
 * These tests verify that the beans are created and configured correctly
 * without loading a full Spring context.
 */
class MessageSourceConfigTest {

    private MessageSourceConfig messageSourceConfig;

    @BeforeEach
    void setUp() {
        // On instancie la classe de configuration directement
        messageSourceConfig = new MessageSourceConfig();
    }

    @Test
    @DisplayName("Le bean messageSource doit être configuré correctement")
    void messageSource_shouldBeConfiguredCorrectly() {
        // GIVEN: un bean MessageSource créé par notre configuration
        MessageSource messageSource = messageSourceConfig.messageSource();

        // THEN:
        // 1. On teste le COMPORTEMENT : on vérifie que le basename est correct en chargeant un message.
        //    Cela prouve que "classpath:/i18n/messages" a été bien configuré, sans dépendre d'un champ privé.
        String message = messageSource.getMessage("NotBlank.test.name", null, Locale.ROOT);
        assertThat(message).isEqualTo("Le nom ne peut pas etre vide (test)");

        // 2. On peut toujours vérifier l'encodage par réflexion si nécessaire, car c'est un autre champ.
        assertThat(ReflectionTestUtils.getField(messageSource, "defaultEncoding")).isEqualTo("UTF-8");
    }

    /**
     * Classe interne simple pour les besoins du test de validation.
     */
    @Setter
    private static class ValidationTestObject {
        @NotBlank(message = "{NotBlank.test.name}")
        private String name;
    }

    @Test
    @DisplayName("Le bean getValidator doit utiliser la MessageSource pour les messages de validation")
    void getValidator_shouldUseMessageSourceForValidationMessages() {
        // GIVEN: un validateur créé par notre configuration
        LocalValidatorFactoryBean validator = messageSourceConfig.getValidator();

        // On déclenche manuellement l'initialisation du bean,
        // ce que Spring ferait automatiquement dans un vrai contexte.
        validator.afterPropertiesSet();

        // Et un objet qui ne respecte pas la contrainte de validation
        ValidationTestObject testObject = new ValidationTestObject();
        testObject.setName(" "); // Invalide car vide

        // WHEN: on valide l'objet
        Set<ConstraintViolation<ValidationTestObject>> violations = validator.validate(testObject);

        // THEN: on vérifie que le message d'erreur vient bien de notre fichier de propriétés,
        // ce qui prouve que la MessageSource a été correctement liée.
        assertThat(violations).hasSize(1);
        ConstraintViolation<ValidationTestObject> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("Le nom ne peut pas etre vide (test)");
    }
}