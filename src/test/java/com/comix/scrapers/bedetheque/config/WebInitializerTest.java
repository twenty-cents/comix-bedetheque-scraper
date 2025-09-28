package com.comix.scrapers.bedetheque.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebInitializerTest {

    @Mock
    private ServletContext mockServletContext;

    @Mock
    private ServletRegistration.Dynamic mockServletRegistration;

    private WebInitializer webInitializer;

    @BeforeEach
    void setUp() {
        webInitializer = new WebInitializer();
        // On configure le mock pour qu'il retourne notre autre mock lorsque addServlet est appelé.
        when(mockServletContext.addServlet(anyString(), any(DispatcherServlet.class)))
                .thenReturn(mockServletRegistration);
    }

    @Test
    @DisplayName("onStartup doit configurer correctement le ServletContext")
    void onStartup_shouldConfigureServletContextCorrectly() {
        // GIVEN
        // Les mocks sont prêts grâce à setUp()

        // WHEN
        // On appelle la méthode à tester
        webInitializer.onStartup(mockServletContext);

        // THEN
        // On utilise des "capteurs" pour capturer les objets passés aux méthodes des mocks
        ArgumentCaptor<ContextLoaderListener> listenerCaptor = ArgumentCaptor.forClass(ContextLoaderListener.class);
        ArgumentCaptor<DispatcherServlet> servletCaptor = ArgumentCaptor.forClass(DispatcherServlet.class);
        ArgumentCaptor<String> servletNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> mappingCaptor = ArgumentCaptor.forClass(String.class);

        // 1. Vérifier qu'un ContextLoaderListener est bien ajouté.
        // Le simple fait de vérifier sa présence est une garantie suffisante et robuste.
        verify(mockServletContext).addListener(listenerCaptor.capture());
        assertThat(listenerCaptor.getValue()).isNotNull();

        // 2. Vérifier que la DispatcherServlet est ajoutée et correctement configurée,
        // car nous POUVONS inspecter son contexte de manière fiable via un getter public.
        verify(mockServletContext).addServlet(servletNameCaptor.capture(), servletCaptor.capture());
        assertThat(servletNameCaptor.getValue()).isEqualTo("dispatcher");
        DispatcherServlet capturedServlet = servletCaptor.getValue();
        assertThat(capturedServlet).isNotNull();

        // On vérifie que la servlet a été créée avec un contexte valide.
        // C'est notre point de vérification principal et fiable pour le contexte.
        WebApplicationContext contextFromServlet = capturedServlet.getWebApplicationContext();
        assertThat(contextFromServlet).isInstanceOf(AnnotationConfigWebApplicationContext.class);
        Assertions.assertNotNull(contextFromServlet);
        assertThat(contextFromServlet.getServletContext()).isEqualTo(mockServletContext);

        // 3. Vérifier la configuration de la servlet
        verify(mockServletRegistration).setLoadOnStartup(1);
        verify(mockServletRegistration).addMapping(mappingCaptor.capture());
        assertThat(mappingCaptor.getValue()).isEqualTo("/");
    }
}