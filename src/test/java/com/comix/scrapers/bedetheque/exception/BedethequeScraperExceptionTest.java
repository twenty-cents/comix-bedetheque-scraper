package com.comix.scrapers.bedetheque.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class BedethequeScraperExceptionTest {

    @Test
    void shouldCreateExceptionWithErrorCode() {
        IErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        BedethequeScraperException exception = new BedethequeScraperException(errorCode);

        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(errorCode.name());
        assertThat(exception.getArgs()).isNull();
    }

    @Test
    void shouldCreateExceptionWithErrorCodeAndArgs() {
        IErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        Object[] args = new Object[]{"123"};
        BedethequeScraperException exception = new BedethequeScraperException(errorCode, args);

        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(errorCode.name());
        assertThat(exception.getArgs()).isEqualTo(args);
    }

    @Test
    void shouldCreateExceptionWithErrorCodeAndMessage() {
        IErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        String message = "Serie not found with id 123";
        BedethequeScraperException exception = new BedethequeScraperException(errorCode, message);

        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getArgs()).isNull();
    }

    @Test
    void shouldCreateExceptionWithErrorCodeAndMessageAndArgs() {
        IErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        String message = "Serie not found with id 123";
        Object[] args = new Object[]{"123"};
        BedethequeScraperException exception = new BedethequeScraperException(errorCode, message, args);

        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getArgs()).isEqualTo(args);
    }

    @Test
    void shouldCreateExceptionWithErrorCodeAndMessageAndCause() {
        IErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        String message = "Serie not found with id 123";
        Throwable cause = new RuntimeException("Cause");
        BedethequeScraperException exception = new BedethequeScraperException(errorCode, message, cause);

        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getArgs()).isNull();
    }

    @Test
    void shouldCreateExceptionWithErrorCodeAndMessageAndCauseAndArgs() {
        IErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        String message = "Serie not found with id 123";
        Throwable cause = new RuntimeException("Cause");
        Object[] args = new Object[]{"123"};
        BedethequeScraperException exception = new BedethequeScraperException(errorCode, message, cause, args);

        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getArgs()).isEqualTo(args);
    }
}
