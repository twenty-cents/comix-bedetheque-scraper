package com.comix.scrapers.bedetheque.exception;

import lombok.Getter;

import java.io.Serial;

@Getter
public class BedethequeScraperException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final transient Object[] args;

    private final transient IErrorCode errorCode;

    public BedethequeScraperException(IErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
        this.args = null;
    }

    public BedethequeScraperException(IErrorCode errorCode, Object[] args) {
        super(errorCode.name());
        this.errorCode = errorCode;
        this.args = args;
    }

    public BedethequeScraperException(IErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
    }

    public BedethequeScraperException(IErrorCode errorCode, String message, Object[] args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }

    public BedethequeScraperException(IErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = null;
    }

    public BedethequeScraperException(IErrorCode errorCode, Throwable cause, Object[] args) {
        super(cause);
        this.errorCode = errorCode;
        this.args = args;
    }

    public BedethequeScraperException(IErrorCode errorCode, String message, Throwable cause, Object[] args) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = args;
    }
}
