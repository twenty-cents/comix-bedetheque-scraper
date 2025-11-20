package com.comix.scrapers.bedetheque.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class NfsVolumeInitializationExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        // Given
        String errorMessage = "NFS server is not available.";

        // When
        var exception = new NfsVolumeInitializationException(errorMessage);

        // Then
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithMessageAndCause() {
        // Given
        String errorMessage = "Failed to mount NFS volume.";
        var cause = new RuntimeException("Underlying I/O error");

        // When
        var exception = new NfsVolumeInitializationException(errorMessage, cause);

        // Then
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
        assertSame(cause, exception.getCause(), "The cause of the exception should be the one provided.");
    }
}