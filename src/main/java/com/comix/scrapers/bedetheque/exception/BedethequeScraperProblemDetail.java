package com.comix.scrapers.bedetheque.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ProblemDetail;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BedethequeScraperProblemDetail extends ProblemDetail {

    private String code;
    private Instant generatedAt;
    private List<ValidationError> errors;

    private BedethequeScraperProblemDetail webClientResponse;

    /**
     * Inner class representing a single validation error.
     */
    @Getter
    @Setter
    public static class ValidationError implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String field;
        private String message;
    }
}