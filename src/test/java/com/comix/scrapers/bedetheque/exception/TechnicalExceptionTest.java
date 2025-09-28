package com.comix.scrapers.bedetheque.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class TechnicalExceptionTest {

    @Test
    @DisplayName("Constructor(codeMessage, cause) should set fields correctly with defaults")
    void constructor_withCodeMessageAndCause_shouldSetFieldsCorrectly() {
        // GIVEN
        String codeMessage = "DB_CONNECTION_FAILED";
        Throwable cause = new RuntimeException("Underlying database error");

        // WHEN
        TechnicalException exception = new TechnicalException(codeMessage, cause);

        // THEN
        assertThat(exception.getCodeMessage()).isEqualTo(codeMessage);
        assertThat(exception.getCause()).isSameAs(cause);
        assertThat(exception.getArgs()).isNull();
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        // Also check the message from the parent RuntimeException
        assertThat(exception.getMessage()).isEqualTo(codeMessage);
    }

    @Test
    @DisplayName("Constructor(codeMessage, cause, args) should set fields correctly with defaults")
    void constructor_withCodeMessageCauseAndArgs_shouldSetFieldsCorrectly() {
        // GIVEN
        String codeMessage = "IO_ERROR";
        Throwable cause = new java.io.IOException("File not found");
        Object[] args = new Object[]{"/path/to/file.txt"};

        // WHEN
        TechnicalException exception = new TechnicalException(codeMessage, cause, args);

        // THEN
        assertThat(exception.getCodeMessage()).isEqualTo(codeMessage);
        assertThat(exception.getCause()).isSameAs(cause);
        assertThat(exception.getArgs()).isEqualTo(args);
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo(codeMessage);
    }

    @Test
    @DisplayName("Constructor(codeMessage, cause, args, httpStatus) should set all fields correctly")
    void constructor_withAllFields_shouldSetAllFieldsCorrectly() {
        // GIVEN
        String codeMessage = "SERVICE_UNAVAILABLE";
        Throwable cause = new InterruptedException("Thread was interrupted");
        Object[] args = new Object[]{"payment-service"};
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;

        // WHEN
        TechnicalException exception = new TechnicalException(codeMessage, cause, args, status);

        // THEN
        assertThat(exception.getCodeMessage()).isEqualTo(codeMessage);
        assertThat(exception.getCause()).isSameAs(cause);
        assertThat(exception.getArgs()).isEqualTo(args);
        assertThat(exception.getHttpStatus()).isEqualTo(status);
        assertThat(exception.getMessage()).isEqualTo(codeMessage);
    }
}