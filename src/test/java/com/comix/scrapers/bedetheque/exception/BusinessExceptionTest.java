package com.comix.scrapers.bedetheque.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class BusinessExceptionTest {

    @Test
    @DisplayName("Constructor(codeMessage) should set fields correctly with defaults")
    void constructor_withCodeMessage_shouldSetFieldsCorrectly() {
        // GIVEN
        String codeMessage = "ERROR_CODE_1";

        // WHEN
        BusinessException exception = new BusinessException(codeMessage);

        // THEN
        assertThat(exception.getCodeMessage()).isEqualTo(codeMessage);
        assertThat(exception.getArgs()).isNull();
        assertThat(exception.getLang()).isNull();
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Constructor(codeMessage, lang) should set fields correctly with defaults")
    void constructor_withCodeMessageAndLang_shouldSetFieldsCorrectly() {
        // GIVEN
        String codeMessage = "ERROR_CODE_2";
        String lang = "fr";

        // WHEN
        BusinessException exception = new BusinessException(codeMessage, lang);

        // THEN
        assertThat(exception.getCodeMessage()).isEqualTo(codeMessage);
        assertThat(exception.getLang()).isEqualTo(lang);
        assertThat(exception.getArgs()).isNull();
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Constructor(codeMessage, lang, httpStatus) should set all fields correctly")
    void constructor_withCodeMessageLangAndStatus_shouldSetFieldsCorrectly() {
        // GIVEN
        String codeMessage = "NOT_FOUND";
        String lang = "en";
        HttpStatus status = HttpStatus.NOT_FOUND;

        // WHEN
        BusinessException exception = new BusinessException(codeMessage, lang, status);

        // THEN
        assertThat(exception.getCodeMessage()).isEqualTo(codeMessage);
        assertThat(exception.getLang()).isEqualTo(lang);
        assertThat(exception.getHttpStatus()).isEqualTo(status);
        assertThat(exception.getArgs()).isNull();
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Constructor(codeMessage, args) should set fields correctly with defaults")
    void constructor_withCodeMessageAndArgs_shouldSetFieldsCorrectly() {
        // GIVEN
        String codeMessage = "INVALID_PARAMETER";
        Object[] args = new Object[]{"username", "password"};

        // WHEN
        BusinessException exception = new BusinessException(codeMessage, args);

        // THEN
        assertThat(exception.getCodeMessage()).isEqualTo(codeMessage);
        assertThat(exception.getArgs()).isEqualTo(args);
        assertThat(exception.getLang()).isNull();
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Constructor(codeMessage, args, httpStatus) should set fields correctly")
    void constructor_withCodeMessageArgsAndStatus_shouldSetFieldsCorrectly() {
        // GIVEN
        String codeMessage = "UNAUTHORIZED";
        Object[] args = new Object[]{"admin_role"};
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        // WHEN
        BusinessException exception = new BusinessException(codeMessage, args, status);

        // THEN
        assertThat(exception.getCodeMessage()).isEqualTo(codeMessage);
        assertThat(exception.getArgs()).isEqualTo(args);
        assertThat(exception.getHttpStatus()).isEqualTo(status);
        assertThat(exception.getLang()).isNull();
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Constructor(codeMessage, args, lang) should set fields correctly with defaults")
    void constructor_withCodeMessageArgsAndLang_shouldSetFieldsCorrectly() {
        // GIVEN
        String codeMessage = "DUPLICATE_ENTRY";
        Object[] args = new Object[]{"user@example.com"};
        String lang = "es";

        // WHEN
        BusinessException exception = new BusinessException(codeMessage, args, lang);

        // THEN
        assertThat(exception.getCodeMessage()).isEqualTo(codeMessage);
        assertThat(exception.getArgs()).isEqualTo(args);
        assertThat(exception.getLang()).isEqualTo(lang);
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Constructor(codeMessage, args, lang, httpStatus) should set all fields correctly")
    void constructor_withAllFields_shouldSetFieldsCorrectly() {
        // GIVEN
        String codeMessage = "CONFLICT";
        Object[] args = new Object[]{"resource_id_123"};
        String lang = "de";
        HttpStatus status = HttpStatus.CONFLICT;

        // WHEN
        BusinessException exception = new BusinessException(codeMessage, args, lang, status);

        // THEN
        assertThat(exception.getCodeMessage()).isEqualTo(codeMessage);
        assertThat(exception.getArgs()).isEqualTo(args);
        assertThat(exception.getLang()).isEqualTo(lang);
        assertThat(exception.getHttpStatus()).isEqualTo(status);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Constructor with cause should set all fields including the cause")
    void constructor_withAllFieldsAndCause_shouldSetAllFields() {
        // GIVEN
        String codeMessage = "INTERNAL_ERROR";
        Object[] args = new Object[]{};
        String lang = "fr";
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        Throwable cause = new IllegalArgumentException("Underlying issue");

        // WHEN
        BusinessException exception = new BusinessException(codeMessage, args, lang, status, cause);

        // THEN
        assertThat(exception.getCodeMessage()).isEqualTo(codeMessage);
        assertThat(exception.getArgs()).isEqualTo(args);
        assertThat(exception.getLang()).isEqualTo(lang);
        assertThat(exception.getHttpStatus()).isEqualTo(status);
        assertThat(exception.getCause()).isSameAs(cause);
    }
}