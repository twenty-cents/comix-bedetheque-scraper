package com.comix.scrapers.bedetheque.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link FilterRegistrationConfig}.
 * <p>
 * These tests verify that if the bean factory method is called,
 * it produces a correctly configured bean. Testing the {@code @ConditionalOnProperty}
 * itself is best done in an integration test.
 */
class FilterRegistrationConfigTest {

    private FilterRegistrationConfig filterRegistrationConfig;

    @BeforeEach
    void setUp() {
        // On instancie la classe de configuration directement pour le test unitaire.
        filterRegistrationConfig = new FilterRegistrationConfig();
    }

    @Test
    @DisplayName("Le bean apiFreebdsFilter doit être configuré correctement")
    void apiFreebdsFilter_shouldBeConfiguredCorrectly() {
        // WHEN: On appelle la méthode qui crée le bean.
        FilterRegistrationBean<CommonsRequestLoggingFilter> registrationBean = filterRegistrationConfig.apiFreebdsFilter();

        // THEN: On vérifie que le bean et son filtre interne sont configurés comme attendu.

        // 1. Vérifier la configuration du FilterRegistrationBean lui-même.
        assertThat(registrationBean).isNotNull();
        assertThat(registrationBean.getUrlPatterns()).containsExactly("/api/bedetheque-scraper/*");

        // 2. Vérifier la configuration du filtre CommonsRequestLoggingFilter.
        CommonsRequestLoggingFilter filter = registrationBean.getFilter();
        assertThat(filter).isNotNull();

        // 3. Vérifier les propriétés du filtre en utilisant la réflexion pour les champs privés/protégés.
        assertThat(ReflectionTestUtils.getField(filter, "includeClientInfo")).isEqualTo(true);
        assertThat(ReflectionTestUtils.getField(filter, "includeQueryString")).isEqualTo(true);
        assertThat(ReflectionTestUtils.getField(filter, "includePayload")).isEqualTo(true);
        assertThat(ReflectionTestUtils.getField(filter, "includeHeaders")).isEqualTo(true);

        // On peut utiliser le getter public pour maxPayloadLength
        assertThat(ReflectionTestUtils.getField(filter, "maxPayloadLength")).isEqualTo(100000);
        assertThat(ReflectionTestUtils.getField(filter, "beforeMessagePrefix")).isEqualTo("incoming request : [");
        assertThat(ReflectionTestUtils.getField(filter, "afterMessagePrefix")).isEqualTo("incoming request body : [");
    }
}