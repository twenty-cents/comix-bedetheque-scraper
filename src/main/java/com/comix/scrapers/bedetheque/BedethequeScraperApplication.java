package com.comix.scrapers.bedetheque;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties
@EnableScheduling
@SpringBootApplication
@EnableSchedulerLock(defaultLockAtMostFor = "1m")
public class BedethequeScraperApplication {

    public static void main(String[] args) {
        SpringApplication.run(BedethequeScraperApplication.class, args);
    }

}
