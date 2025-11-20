package com.comix.scrapers.bedetheque.exception;

/**
 * Exception thrown when an error occurs during the initialization of the NFS volume.
 * This is a runtime exception as such failures are typically fatal during application startup.
 */
public class NfsVolumeInitializationException extends RuntimeException {

    public NfsVolumeInitializationException(String message) {
        super(message);
    }

    public NfsVolumeInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}