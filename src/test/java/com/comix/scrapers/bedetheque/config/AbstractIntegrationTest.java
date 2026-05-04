package com.comix.scrapers.bedetheque.config;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for integration tests that require a PostgreSQL Docker container.
 * Uses @ServiceConnection to automatically configure Spring Data JPA properties.
 */
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class AbstractIntegrationTest {

    /**
     * Using a static container without @Container and @Testcontainers 
     * allows for "Singleton Container" pattern, which is significantly faster 
     * as the database starts once for the entire test suite.
     */
    @ServiceConnection
    protected static final PostgreSQLContainer<?> postgresqlContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withReuse(true);

    static {
        // Start the container manually to ensure it's ready before Spring context initializes
        postgresqlContainer.start();
    }
}