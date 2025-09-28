package com.comix.scrapers.bedetheque.repository;

import com.comix.scrapers.bedetheque.entity.OutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {
    List<OutboxMessage> findByStatus(OutboxMessage.Status status);
}