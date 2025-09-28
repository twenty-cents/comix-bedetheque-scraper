package com.comix.scrapers.bedetheque.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link MvcConfig}.
 * These tests verify that the beans and interceptors are configured correctly
 * without loading a full Spring context.
 */
class MvcConfigTest {

    private MvcConfig mvcConfig;

    @BeforeEach
    void setUp() {
        // On instancie la classe de configuration directement pour les tests unitaires.
        mvcConfig = new MvcConfig();
    }

    @Test
    @DisplayName("Le bean localeResolver doit être un SessionLocaleResolver avec la locale par défaut ENGLISH")
    void localeResolver_shouldBeConfiguredCorrectly() {
        // WHEN: On appelle la méthode qui crée le bean
        LocaleResolver resolver = mvcConfig.localeResolver();

        // THEN: On vérifie que l'objet retourné est du bon type et bien configuré
        assertThat(resolver).isInstanceOf(SessionLocaleResolver.class);

        // La propriété 'defaultLocale' est protégée, on utilise donc la réflexion pour la vérifier.
        Locale defaultLocale = (Locale) ReflectionTestUtils.getField(resolver, "defaultLocale");
        assertThat(defaultLocale).isEqualTo(Locale.ENGLISH);
    }

    @Test
    @DisplayName("Le bean localeChangeInterceptor doit être configuré avec le nom de paramètre 'lang'")
    void localeChangeInterceptor_shouldBeConfiguredCorrectly() {
        // WHEN: On appelle la méthode qui crée le bean
        LocaleChangeInterceptor interceptor = mvcConfig.localeChangeInterceptor();

        // THEN: On vérifie que l'objet retourné est bien configuré
        assertThat(interceptor).isNotNull();
        assertThat(interceptor.getParamName()).isEqualTo("lang");
    }

    @Test
    @DisplayName("La méthode addInterceptors doit enregistrer le localeChangeInterceptor")
    void addInterceptors_shouldRegisterTheLocaleChangeInterceptor() {
        // GIVEN: On crée un mock du registre d'intercepteurs
        InterceptorRegistry registry = mock(InterceptorRegistry.class);
        // On prépare un "capteur" pour capturer l'objet passé à la méthode addInterceptor
        ArgumentCaptor<LocaleChangeInterceptor> interceptorCaptor = ArgumentCaptor.forClass(LocaleChangeInterceptor.class);

        // WHEN: On appelle la méthode à tester
        mvcConfig.addInterceptors(registry);

        // THEN: On vérifie que la méthode addInterceptor du registre a bien été appelée
        // et on capture l'intercepteur qui lui a été passé.
        verify(registry).addInterceptor(interceptorCaptor.capture());

        // On vérifie que l'intercepteur capturé est bien celui que nous attendons.
        LocaleChangeInterceptor capturedInterceptor = interceptorCaptor.getValue();
        assertThat(capturedInterceptor).isNotNull();
        assertThat(capturedInterceptor.getParamName()).isEqualTo("lang");
    }
}