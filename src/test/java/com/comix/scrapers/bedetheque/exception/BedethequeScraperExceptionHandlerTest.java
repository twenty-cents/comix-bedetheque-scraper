package com.comix.scrapers.bedetheque.exception;

import com.comix.scrapers.bedetheque.util.ResourceBundleHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BedethequeScraperExceptionHandlerTest {

    private final BedethequeScraperExceptionHandler exceptionHandler = new BedethequeScraperExceptionHandler();

    @Test
    void handleBedethequeScraperException_shouldReturnCorrectResponseEntity() {
        try (MockedStatic<ResourceBundleHelper> resourceBundleHelperMock = mockStatic(ResourceBundleHelper.class)) {
            // Given
            IErrorCode errorCode = ErrorCode.UNSUPPORTED_ACTION;
            BedethequeScraperException exception = new BedethequeScraperException(errorCode);
            WebRequest webRequest = mock(WebRequest.class);
            when(webRequest.getDescription(false)).thenReturn("uri=/test");

            resourceBundleHelperMock.when(() -> ResourceBundleHelper.getLocalizedMessage(anyString(), (Object[]) any())).thenReturn("Localized Message");

            // When
            ResponseEntity<Object> responseEntity = exceptionHandler.handleBedethequeScraperException(exception, webRequest);

            // Then
            assertThat(responseEntity.getStatusCode()).isEqualTo(errorCode.getHttpStatus());
            Object body = responseEntity.getBody();
            assertThat(body).isInstanceOf(BedethequeScraperProblemDetail.class);

            BedethequeScraperProblemDetail problemDetail = (BedethequeScraperProblemDetail) body;
            Assertions.assertNotNull(problemDetail);
            assertThat(problemDetail.getCode()).isEqualTo(errorCode.name());
            assertThat(problemDetail.getStatus()).isEqualTo(errorCode.getHttpStatus().value());
            assertThat(problemDetail.getTitle()).isEqualTo("Localized Message");
            assertThat(problemDetail.getDetail()).isEqualTo("Localized Message");
            assertThat(problemDetail.getInstance()).isEqualTo(URI.create("/test"));
        }
    }

    @Test
    void handleMethodArgumentNotValid_shouldReturnValidationErrors() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "fieldName", "defaultMessage");

        // Mock the internal chain of calls
        when(exception.getBindingResult()).thenReturn(bindingResult);
        doReturn(List.of(fieldError)).when(bindingResult).getFieldErrors();
        
        WebRequest webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/test");
        HttpHeaders headers = new HttpHeaders();
        HttpStatus status = HttpStatus.BAD_REQUEST;

        // When
        ResponseEntity<Object> responseEntity = exceptionHandler.handleMethodArgumentNotValid(exception, headers, status, webRequest);

        // Then
        Assertions.assertNotNull(responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Object body = responseEntity.getBody();
        assertThat(body).isInstanceOf(BedethequeScraperProblemDetail.class);

        BedethequeScraperProblemDetail problemDetail = (BedethequeScraperProblemDetail) body;
        Assertions.assertNotNull(problemDetail);
        assertThat(problemDetail.getCode()).isEqualTo("VALIDATION_FAILED");
        assertThat(problemDetail.getTitle()).isEqualTo("Validation Failed");

        List<BedethequeScraperProblemDetail.ValidationError> errors = problemDetail.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors.getFirst().getField()).isEqualTo("fieldName");
        assertThat(errors.getFirst().getMessage()).isEqualTo("defaultMessage");
    }

    @Test
    void handleExceptionInternal_shouldFallbackToDefaultProblemDetail() {
        // Given
        Exception exception = new Exception("Generic error");
        WebRequest webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/test");
        HttpHeaders headers = new HttpHeaders();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        // When
        ResponseEntity<Object> responseEntity = exceptionHandler.handleExceptionInternal(exception, null, headers, status, webRequest);

        // Then
        Assertions.assertNotNull(responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        Object body = responseEntity.getBody();
        assertThat(body).isInstanceOf(BedethequeScraperProblemDetail.class);

        BedethequeScraperProblemDetail problemDetail = (BedethequeScraperProblemDetail) body;
        Assertions.assertNotNull(problemDetail);
        assertThat(problemDetail.getCode()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(problemDetail.getStatus()).isEqualTo(500);
        assertThat(problemDetail.getTitle()).isEqualTo("Internal Server Error");
        assertThat(problemDetail.getDetail()).isEqualTo("Generic error");
    }

    @Test
    void handleExceptionInternal_shouldConvertStandardProblemDetail() {
        // Given
        Exception exception = new Exception();
        WebRequest webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/test");
        HttpHeaders headers = new HttpHeaders();
        HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;

        // Create a standard Spring ProblemDetail (simulating what createProblemDetail returns)
        ProblemDetail standardDetail = ProblemDetail.forStatusAndDetail(status, "Method not allowed");
        standardDetail.setTitle("Method Not Allowed");
        standardDetail.setProperty("customProp", "customValue");

        // When
        ResponseEntity<Object> responseEntity = exceptionHandler.handleExceptionInternal(exception, standardDetail, headers, status, webRequest);

        // Then
        Assertions.assertNotNull(responseEntity);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        Object body = responseEntity.getBody();
        assertThat(body).isInstanceOf(BedethequeScraperProblemDetail.class);

        BedethequeScraperProblemDetail problemDetail = (BedethequeScraperProblemDetail) body;
        // Check that properties are copied
        Assertions.assertNotNull(problemDetail);
        assertThat(problemDetail.getStatus()).isEqualTo(405);
        assertThat(problemDetail.getTitle()).isEqualTo("Method Not Allowed");
        assertThat(problemDetail.getDetail()).isEqualTo("Method not allowed");
        assertThat(problemDetail.getProperties()).containsEntry("customProp", "customValue");
        assertThat(problemDetail.getGeneratedAt()).isNotNull();
    }
}
