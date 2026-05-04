package com.comix.scrapers.bedetheque.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum ErrorCode implements IErrorCode {

    UNSUPPORTED_ACTION(HttpStatus.BAD_REQUEST),
    LETTER_NOT_FOUND(HttpStatus.BAD_REQUEST),
    MEDIA_DOWNLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    MEDIA_DIRECTORY_CREATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    MEDIA_RESOURCE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR),
    MEDIA_RESOURCE_ALREADY_EXISTS(HttpStatus.INTERNAL_SERVER_ERROR),
    MEDIA_RESOURCE_SAVE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    MEDIA_SCRAPING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    AUTOCOMPLETE_SCRAPING_ERROR(HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    @Getter
    private final HttpStatus httpStatus;

    ErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}
