package com.comix.scrapers.bedetheque.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ShedLockConfigTest {

    @Autowired
    private LockProvider lockProvider;

    @Test
    @DisplayName("Should create a JdbcTemplateLockProvider bean")
    void shouldCreateJdbcTemplateLockProvider() {
        assertThat(lockProvider).isNotNull().isInstanceOf(JdbcTemplateLockProvider.class);
    }
}