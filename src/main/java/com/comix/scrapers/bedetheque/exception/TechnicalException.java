package com.comix.scrapers.bedetheque.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TechnicalException extends RuntimeException {

    private final String codeMessage;
    private final Object[] args; //NOSONAR
    private final HttpStatus httpStatus;

    public TechnicalException(String codeMessage, Throwable cause) {
        super(codeMessage, cause);
        this.codeMessage = codeMessage;
        this.args = null;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public TechnicalException(String codeMessage, Throwable cause, Object[] args) {
        super(codeMessage, cause);
        this.codeMessage = codeMessage;
        this.args = args;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public TechnicalException(String codeMessage, Throwable cause, Object[] args, HttpStatus httpStatus) {
        super(codeMessage, cause);
        this.codeMessage = codeMessage;
        this.args = args;
        this.httpStatus = httpStatus;
    }
}
