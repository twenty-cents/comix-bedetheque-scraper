package com.comix.scrapers.bedetheque.exception;


import com.comix.scrapers.bedetheque.util.ResourceBundleHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestControllerAdvice
@Slf4j
public class BedethequeScraperExceptionHandler extends ResponseEntityExceptionHandler {

    // Pattern pour identifier les accents après normalisation NFD
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    public BedethequeScraperExceptionHandler() {
        // Default constructor
    }

    /**
     * Handles validation exceptions for request bodies (@Valid).
     */
    @Override
    @SuppressWarnings("java:S2638")
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {

        BedethequeScraperProblemDetail problemDetail = new BedethequeScraperProblemDetail();
        problemDetail.setGeneratedAt(Instant.now());
        problemDetail.setStatus(status.value());
        problemDetail.setTitle("Validation Failed");
        problemDetail.setDetail("The request body is invalid. See errors for details.");
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        problemDetail.setCode("VALIDATION_FAILED");

        List<BedethequeScraperProblemDetail.ValidationError> validationErrors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            BedethequeScraperProblemDetail.ValidationError error = new BedethequeScraperProblemDetail.ValidationError();
            error.setField(fieldError.getField());
            error.setMessage(fieldError.getDefaultMessage());
            validationErrors.add(error);
        });
        problemDetail.setErrors(validationErrors);

        return handleExceptionInternal(ex, problemDetail, headers, status, request);
    }

    @ExceptionHandler(BedethequeScraperException.class)
    public ResponseEntity<Object> handleBedethequeScraperException(BedethequeScraperException ex, WebRequest request) {
        String title = ResourceBundleHelper.getLocalizedMessage("error." + ex.getErrorCode().name() + ".title", ex.getArgs());
        String details = ResourceBundleHelper.getLocalizedMessage("error." + ex.getErrorCode().name() + ".detail", ex.getArgs());
        HttpStatus httpStatus = ex.getErrorCode().getHttpStatus();

        BedethequeScraperProblemDetail problemDetail = new BedethequeScraperProblemDetail();
        problemDetail.setGeneratedAt(Instant.now());
        problemDetail.setStatus(httpStatus.value());
        problemDetail.setCode(ex.getErrorCode().name());
        problemDetail.setTitle(title);
        problemDetail.setDetail(details);
        problemDetail.setType(URI.create("/errors/bedetheque-manager-exception"));
        problemDetail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));

        return handleExceptionInternal(ex, problemDetail, new HttpHeaders(), httpStatus, request);
    }

    /**
     * Final hook for all outgoing IErrorCode Responses from the handler.
     * Overriding this ensures that even responses not created via createProblemDetail
     * are consistently formatted.
     */
    @Override
    @SuppressWarnings("java:S2638")
    protected ResponseEntity<Object> handleExceptionInternal(
            @NonNull Exception ex, @Nullable Object body, @NonNull HttpHeaders headers, @NonNull HttpStatusCode statusCode, @NonNull WebRequest request) {

        Object responseBody = body;

        // If body is null, check if ex implements ErrorResponse
        if (responseBody == null && ex instanceof ErrorResponse errorResponse) {
            responseBody = errorResponse.getBody();
        }

        BedethequeScraperProblemDetail problemDetail = null;

        if (responseBody instanceof BedethequeScraperProblemDetail bedethequeManagerProblemDetail) {
            problemDetail = bedethequeManagerProblemDetail;
        }

        if (problemDetail == null && responseBody instanceof ProblemDetail detail) {
            problemDetail = convertToBedethequeScraperProblemDetail(detail);
        }

        if (problemDetail == null) {
            // Fallback
            problemDetail = new BedethequeScraperProblemDetail();
            problemDetail.setType(URI.create("about:blank"));
            String reasonPhrase = HttpStatus.valueOf(statusCode.value()).getReasonPhrase();
            problemDetail.setTitle(reasonPhrase);
            problemDetail.setCode(toSnakeCase(reasonPhrase));
            problemDetail.setStatus(statusCode.value());
            problemDetail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
            problemDetail.setDetail(ex.getMessage());
            problemDetail.setGeneratedAt(Instant.now());
        }

        logger.error(ex.getMessage(), ex);
        return super.handleExceptionInternal(ex, problemDetail, headers, statusCode, request);
    }

    /**
     * Internal factory method to transform a standard ProblemDetail into our BedethequeScraperProblemDetail.
     */
    private BedethequeScraperProblemDetail convertToBedethequeScraperProblemDetail(ProblemDetail detail) {
        BedethequeScraperProblemDetail custom = new BedethequeScraperProblemDetail();
        custom.setGeneratedAt(Instant.now());
        custom.setStatus(detail.getStatus());
        custom.setTitle(detail.getTitle());
        custom.setDetail(detail.getDetail());
        custom.setType(detail.getType());
        custom.setInstance(detail.getInstance());
        if (detail.getTitle() != null) {
            custom.setCode(toSnakeCase(detail.getTitle()));
        }

        Map<String, Object> properties = detail.getProperties();
        if (properties != null) {
            properties.forEach(custom::setProperty);
        }
        return custom;
    }

    private String toSnakeCase(String input) {
        if (input == null) return "";

        // 1. Décomposition des accents (NFD)
        String decomposed = Normalizer.normalize(input, Normalizer.Form.NFD);
        // 2. Suppression des accents
        input = DIACRITICS.matcher(decomposed).replaceAll("");
        return input
                .replaceAll("([a-z])([A-Z]+)", "$1_$2") // camelCase to snake_case
                .replace(' ', '_')
                .replace('-', '_')
                .toUpperCase();
    }
}