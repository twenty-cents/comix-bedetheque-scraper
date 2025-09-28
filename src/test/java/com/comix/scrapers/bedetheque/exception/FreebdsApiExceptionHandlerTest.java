package com.comix.scrapers.bedetheque.exception;

import com.comix.scrapers.bedetheque.rest.v1.dto.ApiErrorDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.FieldErrorDto;
import com.comix.scrapers.bedetheque.util.ApiLanguageHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FreebdsApiExceptionHandlerTest {

    @Mock
    private ApiLanguageHelper apiLanguageHelper;

    @InjectMocks
    private FreebdsApiExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        // Nothing to do here
    }

    @Nested
    @DisplayName("Tests pour handleMethodArgumentNotValid")
    class HandleMethodArgumentNotValid {

        // Méthode factice nécessaire pour créer une instance de MethodParameter
        public void dummyMethodForTest(String param) {
            // Nothing to do here
        }

        @Test
        @DisplayName("doit retourner 400 Bad Request avec les erreurs de champ")
        void shouldReturnBadRequestWithFieldErrors() throws NoSuchMethodException {
            // GIVEN
            WebRequest webRequest = mock(WebRequest.class);
            when(webRequest.getParameter("lang")).thenReturn("en");

            BindingResult bindingResult = mock(BindingResult.class);
            FieldError fieldError = new FieldError("myObject", "username", "must not be blank");
            when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
            when(bindingResult.getGlobalErrors()).thenReturn(Collections.emptyList());

            MethodParameter parameter = new MethodParameter(this.getClass().getDeclaredMethod("dummyMethodForTest", String.class), 0);
            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

            // WHEN
            when(apiLanguageHelper.validateAndApplyApiLanguage(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

            ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentNotValid(
                    ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest
            );

            // THEN
            Assertions.assertNotNull(response);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isInstanceOf(ApiErrorDto.class);

            ApiErrorDto apiError = (ApiErrorDto) response.getBody();
            Assertions.assertNotNull(apiError);
            assertThat(apiError.getCode()).isEqualTo("ERR-CTL-ARG");
            assertThat(apiError.getMessage()).isEqualTo("Invalid arguments provided."); // Message du fichier de test .properties
            assertThat(apiError.getDetail()).hasSize(1);

            FieldErrorDto errorDetail = apiError.getDetail().getFirst();
            assertThat(errorDetail.getField()).isEqualTo("username");
            assertThat(errorDetail.getMessage()).isEqualTo("must not be blank");
        }
    }

    @Nested
    @DisplayName("Tests pour handleConstraintViolationException")
    class HandleConstraintViolationException {

        @Test
        @DisplayName("doit retourner 400 Bad Request avec les détails de la violation")
        void shouldReturnBadRequestWithViolationDetails() {
            // GIVEN
            ConstraintViolation<?> violation = mock(ConstraintViolation.class);
            Path path = mock(Path.class);
            when(path.toString()).thenReturn("url");
            when(violation.getPropertyPath()).thenReturn(path);
            when(violation.getMessage()).thenReturn("must be a valid URL");

            ConstraintViolationException ex = new ConstraintViolationException("Validation failed", Set.of(violation));

            // WHEN
            ResponseEntity<Object> response = exceptionHandler.handleFreeBdsApiScraperBusinessException(ex);

            // THEN
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isInstanceOf(ApiErrorDto.class);

            ApiErrorDto apiError = (ApiErrorDto) response.getBody();
            Assertions.assertNotNull(apiError);
            assertThat(apiError.getCode()).isEqualTo("CONSTRAINT_VIOLATION");
            assertThat(apiError.getMessage()).isEqualTo("A constraint violation occurred."); // Message du fichier de test .properties
            assertThat(apiError.getDetail()).hasSize(1);

            FieldErrorDto errorDetail = apiError.getDetail().getFirst();
            assertThat(errorDetail.getField()).isEqualTo("url");
            assertThat(errorDetail.getMessage()).isEqualTo("must be a valid URL");
        }
    }

    @Nested
    @DisplayName("Tests pour handleTechnicalException")
    class HandleTechnicalException {

        @Test
        @DisplayName("doit retourner le statut HTTP et le message de l'exception")
        void shouldReturnStatusAndMessageFromException() {
            // GIVEN
            String errorCode = "DB_ERROR";
            Object[] args = new Object[]{"connection timeout"};
            Throwable cause = new RuntimeException("timeout");
            TechnicalException ex = new TechnicalException(errorCode, cause, args, HttpStatus.SERVICE_UNAVAILABLE);

            // WHEN
            ResponseEntity<Object> response = exceptionHandler.handleFreeBdsApiScraperTechnicalException(ex);

            // THEN
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
            assertThat(response.getBody()).isInstanceOf(ApiErrorDto.class);

            ApiErrorDto apiError = (ApiErrorDto) response.getBody();
            Assertions.assertNotNull(apiError);
            assertThat(apiError.getCode()).isEqualTo(errorCode);
            assertThat(apiError.getType()).isEqualTo("Technical exception");
            assertThat(apiError.getMessage()).isEqualTo("A database error occurred: connection timeout");
            assertThat(apiError.getDetail()).isNullOrEmpty();
        }
    }

    @Nested
    @DisplayName("Tests pour handleBusinessException")
    class HandleBusinessException {

        @Test
        @DisplayName("doit retourner le statut HTTP et le message de l'exception")
        void shouldReturnStatusAndMessageFromException() {
            // GIVEN
            String errorCode = "UNSUPPORTED_ACTION";
            Object[] args = new Object[]{"DELETE"};
            BusinessException ex = new BusinessException(errorCode, args, "en", HttpStatus.NOT_IMPLEMENTED);

            // WHEN
            ResponseEntity<Object> response = exceptionHandler.handleFreeBdsApiScraperBusinessException(ex);

            // THEN
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
            assertThat(response.getBody()).isInstanceOf(ApiErrorDto.class);

            ApiErrorDto apiError = (ApiErrorDto) response.getBody();
            Assertions.assertNotNull(apiError);
            assertThat(apiError.getCode()).isEqualTo(errorCode);
            assertThat(apiError.getType()).isEqualTo("Business exception");
            assertThat(apiError.getMessage()).isEqualTo("Action DELETE is not supported.");
            assertThat(apiError.getDetail()).isNullOrEmpty();
        }
    }

    @Nested
    @DisplayName("Tests pour handleException (Générique)")
    class HandleGenericException {

        @Test
        @DisplayName("doit retourner 400 Bad Request pour une exception générique")
        void shouldReturnBadRequestForGenericException() {
            // GIVEN
            Exception ex = new Exception("An unexpected error occurred");
            HttpServletRequest request = mock(HttpServletRequest.class);

            // WHEN
            ResponseEntity<Object> response = exceptionHandler.handleException(ex, request);

            // THEN
            // NOTE : Le code actuel retourne BAD_REQUEST. Une meilleure pratique serait de retourner INTERNAL_SERVER_ERROR (500).
            // Ce test vérifie le comportement actuel.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isInstanceOf(ApiErrorDto.class);

            ApiErrorDto apiError = (ApiErrorDto) response.getBody();
            Assertions.assertNotNull(apiError);
            assertThat(apiError.getCode()).isEqualTo("ERR-JVM-001");
            assertThat(apiError.getType()).isEqualTo("Exception");
            assertThat(apiError.getMessage()).isEqualTo("An unexpected error occurred");
            assertThat(apiError.getDetail()).isNullOrEmpty();
        }
    }
}