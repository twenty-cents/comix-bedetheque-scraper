package com.comix.scrapers.bedetheque.client.scraper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(initializers = ConfigDataApplicationContextInitializer.class)
class GlobalStatisticsScraperIT {

    @Value("${bedetheque.url.home}")
    private String bedethequeUrl;

    @Value("${application.downloads.localcache.active}")
    private boolean isLocalCacheActive;

    @Value("#{new Long('${application.scraping.latency}')}")
    private Long latency;

    @Test
    void scrapShouldReturnOk() {
        GlobalStatisticsScraper globalStatisticsScraper = new GlobalStatisticsScraper();
        globalStatisticsScraper.setBedethequeUrl(bedethequeUrl);
        globalStatisticsScraper.setLocalCacheActive(isLocalCacheActive);
        globalStatisticsScraper.setLatency(latency);

        var globalStatistics = globalStatisticsScraper.scrap();
        assertThat(globalStatistics.getSeries()).isPositive();
        assertThat(globalStatistics.getGraphicNovels()).isPositive();
        assertThat(globalStatistics.getAuthors()).isPositive();
        assertThat(globalStatistics.getReviews()).isPositive();
        assertThat(globalStatistics.getLastEntries()).isNotEmpty();
        assertThat(globalStatistics.getNews()).isNotEmpty();
    }
}