package com.comix.scrapers.bedetheque.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingFilterTest {

    private static final String TRACEID_HEADER = "X-B3-TraceId";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private LoggingFilter loggingFilter;

    @Test
    @DisplayName("Doit générer un nouveau TraceId si aucun n'est fourni")
    void doFilter_shouldGenerateNewTraceId_whenNoneIsProvided() throws IOException, ServletException {
        // GIVEN
        // Simule une requête sans en-tête de trace
        when(request.getHeader("X-FORWARD-FOR")).thenReturn("192.168.1.1");
        when(request.getHeader("user-agent")).thenReturn("Test-Agent/1.0");
        when(request.getHeader(TRACEID_HEADER)).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET");

        // On utilise un try-with-resources pour mocker la classe statique MDC
        try (MockedStatic<MDC> mdcMock = Mockito.mockStatic(MDC.class)) {
            // WHEN
            loggingFilter.doFilter(request, response, filterChain);

            // THEN
            // 1. Vérifie que le MDC a été peuplé avec un traceId généré
            mdcMock.verify(() -> MDC.put(eq("traceId"), anyString()));

            // 2. Capture et vérifie que l'en-tête de réponse contient un traceId
            ArgumentCaptor<String> traceIdCaptor = ArgumentCaptor.forClass(String.class);
            verify(response).setHeader(eq(TRACEID_HEADER), traceIdCaptor.capture());
            assertThat(traceIdCaptor.getValue()).isNotNull();
            // Vérifie que la valeur ressemble à un UUID
            assertThatCode(() -> UUID.fromString(traceIdCaptor.getValue())).doesNotThrowAnyException();

            // 3. Vérifie que la chaîne de filtres a été appelée
            verify(filterChain, times(1)).doFilter(any(TraceableServletRequestWrapper.class), eq(response));

            // 4. Vérifie que le MDC a été nettoyé
            mdcMock.verify(MDC::clear, times(1));
        }
    }

    @Test
    @DisplayName("Doit utiliser le TraceId existant s'il est fourni dans l'en-tête")
    void doFilter_shouldUseExistingTraceId_whenHeaderIsProvided() throws IOException, ServletException {
        // GIVEN
        String existingTraceId = "existing-trace-id-123";
        when(request.getHeader("X-FORWARD-FOR")).thenReturn("192.168.1.1");
        when(request.getHeader("user-agent")).thenReturn("Test-Agent/1.0");
        when(request.getHeader(TRACEID_HEADER)).thenReturn(existingTraceId);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET");

        try (MockedStatic<MDC> mdcMock = Mockito.mockStatic(MDC.class)) {
            // WHEN
            loggingFilter.doFilter(request, response, filterChain);

            // THEN
            // 1. Vérifie que le MDC a été peuplé avec le traceId existant
            mdcMock.verify(() -> MDC.put("traceId", existingTraceId));

            // 2. Vérifie que l'en-tête de réponse contient le même traceId
            verify(response).setHeader(TRACEID_HEADER, existingTraceId);

            // 3. Vérifie que la chaîne de filtres a été appelée
            verify(filterChain, times(1)).doFilter(any(TraceableServletRequestWrapper.class), eq(response));

            // 4. Vérifie que le MDC a été nettoyé
            mdcMock.verify(MDC::clear, times(1));
        }
    }

    @Test
    @DisplayName("Doit correctement définir les informations de la requête dans le MDC")
    void doFilter_shouldSetAllRequestInfoInMdc() throws IOException, ServletException {
        // GIVEN
        when(request.getHeader("X-FORWARD-FOR")).thenReturn("192.168.1.1");
        when(request.getHeader("user-agent")).thenReturn("Test-Agent/1.0");
        when(request.getRequestURI()).thenReturn("/api/resource");
        when(request.getMethod()).thenReturn("POST");

        try (MockedStatic<MDC> mdcMock = Mockito.mockStatic(MDC.class)) {
            // WHEN
            loggingFilter.doFilter(request, response, filterChain);

            // THEN
            mdcMock.verify(() -> MDC.put("userIP", "192.168.1.1"));
            mdcMock.verify(() -> MDC.put("user-agent", "Test-Agent/1.0"));
            mdcMock.verify(() -> MDC.put("requestURI", "/api/resource"));
            mdcMock.verify(() -> MDC.put("method", "POST"));
            mdcMock.verify(() -> MDC.put(eq("hostname"), anyString()));
        }
    }

    @Test
    @DisplayName("Doit nettoyer le MDC même si une exception est levée dans la chaîne de filtres")
    void doFilter_shouldClearMdc_whenChainThrowsException() throws IOException, ServletException {
        // GIVEN
        // Configure la chaîne de filtres pour qu'elle lève une exception
        doThrow(new IOException("Something went wrong down the chain")).when(filterChain).doFilter(any(ServletRequest.class), any());

        try (MockedStatic<MDC> mdcMock = Mockito.mockStatic(MDC.class)) {
            // WHEN & THEN
            // Vérifie que l'exception est bien propagée
            assertThatThrownBy(() -> loggingFilter.doFilter(request, response, filterChain))
                    .isInstanceOf(IOException.class)
                    .hasMessage("Something went wrong down the chain");

            // Le test le plus important : vérifie que MDC.clear() est quand même appelé grâce au bloc finally
            mdcMock.verify(MDC::clear, times(1));
        }
    }
}