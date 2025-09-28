package com.comix.scrapers.bedetheque.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraceableServletRequestWrapperTest {

    @Mock
    private HttpServletRequest mockRequest;

    private TraceableServletRequestWrapper requestWrapper;

    @BeforeEach
    void setUp() {
        requestWrapper = new TraceableServletRequestWrapper(mockRequest);
    }

    @Nested
    @DisplayName("Tests pour getHeader(name)")
    class GetHeaderTests {

        @Test
        @DisplayName("doit retourner la valeur de l'en-tête original s'il n'est pas surchargé")
        void shouldReturnOriginalHeaderWhenNotOverridden() {
            when(mockRequest.getHeader("Original-Header")).thenReturn("original-value");
            assertThat(requestWrapper.getHeader("Original-Header")).isEqualTo("original-value");
        }

        @Test
        @DisplayName("doit retourner la nouvelle valeur pour un en-tête ajouté")
        void shouldReturnAddedHeaderValue() {
            requestWrapper.addHeader("New-Header", "new-value");
            assertThat(requestWrapper.getHeader("New-Header")).isEqualTo("new-value");
        }

        @Test
        @DisplayName("doit retourner la valeur surchargée même si l'en-tête existe dans l'original")
        void shouldReturnOverriddenHeaderValue() {
            requestWrapper.addHeader("X-B3-TraceId", "new-trace-id");
            assertThat(requestWrapper.getHeader("X-B3-TraceId")).isEqualTo("new-trace-id");
        }

        @Test
        @DisplayName("doit retourner null pour un en-tête inexistant")
        void shouldReturnNullForNonExistentHeader() {
            when(mockRequest.getHeader("Non-Existent")).thenReturn(null);
            assertThat(requestWrapper.getHeader("Non-Existent")).isNull();
        }
    }

    @Nested
    @DisplayName("Tests pour getHeaderNames()")
    class GetHeaderNamesTests {

        @Test
        @DisplayName("doit retourner une liste combinée et unique des noms d'en-tête")
        void shouldReturnCombinedAndUniqueHeaderNames() {
            // GIVEN
            // En-têtes originaux
            when(mockRequest.getHeaderNames()).thenReturn(Collections.enumeration(List.of("Accept", "Content-Type")));
            // En-tête ajouté qui surcharge un existant et un nouveau
            requestWrapper.addHeader("Content-Type", "application/json");
            requestWrapper.addHeader("X-B3-TraceId", "some-id");

            // WHEN
            List<String> headerNames = Collections.list(requestWrapper.getHeaderNames());

            // THEN
            // La liste doit contenir tous les noms, sans doublons.
            assertThat(headerNames).containsExactlyInAnyOrder("Accept", "Content-Type", "X-B3-TraceId");
        }
    }

    @Nested
    @DisplayName("Tests pour getHeaders(name)")
    class GetHeadersTests {

        @Test
        @DisplayName("doit retourner les valeurs de l'en-tête original s'il n'est pas surchargé")
        void shouldReturnOriginalHeaderValuesWhenNotOverridden() {
            when(mockRequest.getHeaders("Accept-Language")).thenReturn(Collections.enumeration(List.of("fr-FR", "en-US")));
            assertThat(Collections.list(requestWrapper.getHeaders("Accept-Language"))).containsExactly("fr-FR", "en-US");
        }

        @Test
        @DisplayName("doit retourner seulement la nouvelle valeur pour un en-tête surchargé")
        void shouldReturnOnlyOverriddenValueForHeader() {
            // GIVEN
            // Le wrapper ajoute la nouvelle valeur
            requestWrapper.addHeader("X-B3-TraceId", "new-trace-id");

            // WHEN
            List<String> headerValues = Collections.list(requestWrapper.getHeaders("X-B3-TraceId"));

            // THEN
            // Seule la nouvelle valeur doit être retournée, prouvant le remplacement.
            assertThat(headerValues).containsExactly("new-trace-id");
        }
    }
}