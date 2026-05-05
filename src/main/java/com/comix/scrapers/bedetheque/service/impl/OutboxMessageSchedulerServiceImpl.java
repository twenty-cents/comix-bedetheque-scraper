package com.comix.scrapers.bedetheque.service.impl;

import com.comix.scrapers.bedetheque.entity.OutboxMessage;
import com.comix.scrapers.bedetheque.repository.OutboxMessageRepository;
import com.comix.scrapers.bedetheque.service.OutboxMessageSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class OutboxMessageSchedulerServiceImpl implements OutboxMessageSchedulerService {
    
    private final OutboxMessageRepository outboxMessageRepository;

    /**
     * Number of days to keep OutboxMessage entities. Configured via application.yml.
     * Defaults to 30 days if not specified.
     */
    @Value("${application.purge.outbox-messages.days-to-keep:30}")
    private int daysToKeepOutboxMessages;

    public OutboxMessageSchedulerServiceImpl(OutboxMessageRepository outboxMessageRepository) {
        this.outboxMessageRepository = outboxMessageRepository;
    }

    /**
     * Scheduled method to purge old OutboxMessage entities.
     * The cron expression is configured via application.yml (defaults to daily at 2 AM).
     */
    @Override
    @Scheduled(cron = "${application.purge.outbox-messages.cron:0 0 2 * * ?}")
    @Transactional
    public void purgeOldOutboxMessages() {
        log.info("Starting purge of SENT OutboxMessage entities older than {} days...", daysToKeepOutboxMessages);
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeepOutboxMessages);
        int deletedCount = outboxMessageRepository.deleteByStatusAndCreatedAtBefore(OutboxMessage.Status.SENT, cutoffDate);
        log.info("Purged {} SENT OutboxMessage entities created before {}", deletedCount, cutoffDate);
    }
}
