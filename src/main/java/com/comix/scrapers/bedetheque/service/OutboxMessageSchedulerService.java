package com.comix.scrapers.bedetheque.service;

public interface OutboxMessageSchedulerService {

    void purgeOldOutboxMessages();
}
