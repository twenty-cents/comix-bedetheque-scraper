package com.comix.scrapers.bedetheque.exception;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class BedethequeScraperProblemDetailTest {

    @Test
    void shouldCreateProblemDetailWithProperties() {
        BedethequeScraperProblemDetail problemDetail = new BedethequeScraperProblemDetail();
        Instant now = Instant.now();
        problemDetail.setGeneratedAt(now);
        problemDetail.setCode("ERROR_CODE");
        problemDetail.setErrors(Collections.emptyList());

        assertThat(problemDetail.getGeneratedAt()).isEqualTo(now);
        assertThat(problemDetail.getCode()).isEqualTo("ERROR_CODE");
        assertThat(problemDetail.getErrors()).isEmpty();
    }

    @Test
    void shouldCreateValidationError() {
        BedethequeScraperProblemDetail.ValidationError error = new BedethequeScraperProblemDetail.ValidationError();
        error.setField("field");
        error.setMessage("message");

        assertThat(error.getField()).isEqualTo("field");
        assertThat(error.getMessage()).isEqualTo("message");
    }
}
