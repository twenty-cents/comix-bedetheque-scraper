package com.comix.scrapers.bedetheque.service.impl;

import com.comix.scrapers.bedetheque.entity.OutboxMessage;
import com.comix.scrapers.bedetheque.repository.OutboxMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxMessageSchedulerServiceImplTest {

    @Mock
    private OutboxMessageRepository outboxMessageRepository;

    @InjectMocks
    private OutboxMessageSchedulerServiceImpl outboxMessageSchedulerService;

    private static final int DAYS_TO_KEEP = 30;
    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2024, 5, 15, 10, 0, 0);

    @BeforeEach
    void setUp() {
        // Inject the @Value properties using ReflectionTestUtils as @Value is not processed in unit tests
        ReflectionTestUtils.setField(outboxMessageSchedulerService, "daysToKeepOutboxMessages", DAYS_TO_KEEP);
    }

    @Test
    @DisplayName("Should purge SENT outbox messages older than configured days")
    void purgeOldOutboxMessages_shouldDeleteSentMessages() {
        // Given
        int deletedCount = 5;
        ArgumentCaptor<LocalDateTime> cutoffDateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        // Mock LocalDateTime.now() to return a fixed time for deterministic testing
        // CALLS_REAL_METHODS is essential so that other static methods like LocalDateTime.of() still work
        try (MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(FIXED_NOW);

            when(outboxMessageRepository.deleteByStatusAndCreatedAtBefore(eq(OutboxMessage.Status.SENT), any(LocalDateTime.class)))
                    .thenReturn(deletedCount);

            // When
            outboxMessageSchedulerService.purgeOldOutboxMessages();

            // Then
            // Verify and capture the argument
            verify(outboxMessageRepository).deleteByStatusAndCreatedAtBefore(
                    eq(OutboxMessage.Status.SENT),
                    cutoffDateCaptor.capture());

            // Verify that the captured cutoff date is correct based on the fixed time and days to keep
            LocalDateTime expectedCutoffDate = FIXED_NOW.minusDays(DAYS_TO_KEEP);
            assertThat(cutoffDateCaptor.getValue()).isEqualTo(expectedCutoffDate);
        }
    }

    @Test
    @DisplayName("Should call repository even if no messages are purged")
    void purgeOldOutboxMessages_shouldCallRepositoryEvenIfNoMessagesPurged() {
        // Given
        int deletedCount = 0; // Simulate no messages being deleted

        // Mock LocalDateTime.now() to return a fixed time for deterministic testing
        try (MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(FIXED_NOW);

            when(outboxMessageRepository.deleteByStatusAndCreatedAtBefore(eq(OutboxMessage.Status.SENT), any(LocalDateTime.class)))
                    .thenReturn(deletedCount);

            // When
            outboxMessageSchedulerService.purgeOldOutboxMessages();

            // Then
            // Verify that the repository method was still called, even if it returned 0 deletions
            verify(outboxMessageRepository).deleteByStatusAndCreatedAtBefore(
                    eq(OutboxMessage.Status.SENT),
                    any(LocalDateTime.class));
        }
    }
}