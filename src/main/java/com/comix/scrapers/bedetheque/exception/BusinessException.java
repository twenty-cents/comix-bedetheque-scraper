package com.comix.scrapers.bedetheque.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final String codeMessage;
    private final Object[] args; //NOSONAR
    private final String lang;
    private final HttpStatus httpStatus;

    public BusinessException(String codeMessage) {
        this.codeMessage = codeMessage;
        this.args = null;
        this.lang = null;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public BusinessException(String codeMessage, String lang) {
        this.codeMessage = codeMessage;
        this.args = null;
        this.lang = lang;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public BusinessException(String codeMessage, String lang, HttpStatus httpStatus) {
        this.codeMessage = codeMessage;
        this.args = null;
        this.lang = lang;
        this.httpStatus = httpStatus;
    }

    public BusinessException(String codeMessage, Object[] args) {
        this.codeMessage = codeMessage;
        this.args = args;
        this.lang = null;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public BusinessException(String codeMessage, Object[] args, HttpStatus httpStatus) {
        this.codeMessage = codeMessage;
        this.args = args;
        this.lang = null;
        this.httpStatus = httpStatus;
    }

    public BusinessException(String codeMessage, Object[] args, String lang) {
        this.codeMessage = codeMessage;
        this.args = args;
        this.lang = lang;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public BusinessException(String codeMessage, Object[] args, String lang, HttpStatus httpStatus) {
        this.codeMessage = codeMessage;
        this.args = args;
        this.lang = lang;
        this.httpStatus = httpStatus;
    }

    public BusinessException(String codeMessage, Object[] args, String lang, HttpStatus httpStatus, Throwable cause) {
        super(cause);
        this.codeMessage = codeMessage;
        this.args = args;
        this.lang = lang;
        this.httpStatus = httpStatus;
    }
}
