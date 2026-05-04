package com.comix.scrapers.bedetheque.exception;

import org.springframework.http.HttpStatus;

public interface IErrorCode {

    String name();

    HttpStatus getHttpStatus();

    default String title() {
        return "error." + name() + ".title";
    }

    default String details() {
        return "error." + name() + ".details";
    }

}
