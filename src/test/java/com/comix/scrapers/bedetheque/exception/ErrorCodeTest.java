package com.comix.scrapers.bedetheque.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorCodeTest {

    @Test
    void shouldReturnCorrectTitleAndDetails() {
        for (ErrorCode errorCode : ErrorCode.values()) {
            assertThat(errorCode.title()).isEqualTo("error." + errorCode.name() + ".title");
            assertThat(errorCode.details()).isEqualTo("error." + errorCode.name() + ".details");
        }
    }

    @Test
    void shouldReturnCorrectHttpStatus() {
        assertThat(ErrorCode.UNSUPPORTED_ACTION.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
