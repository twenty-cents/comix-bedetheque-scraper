package com.comix.scrapers.bedetheque.repository;

import com.comix.scrapers.bedetheque.entity.OutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {

    /**
     * Deletes OutboxMessage entities that were created before the specified cutoff date.
     *
     * @param cutoffDate The date before which messages should be deleted.
     * @return The number of entities deleted.
     */
    @Modifying
    @Transactional // Ensure this delete operation runs in a transaction
    int deleteByStatusAndCreatedAtBefore(OutboxMessage.Status status, LocalDateTime cutoffDate);

    List<OutboxMessage> findByStatus(OutboxMessage.Status status);
}