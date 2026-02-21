package com.comix.scrapers.bedetheque.client.scraper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(initializers = ConfigDataApplicationContextInitializer.class)
class GlobalStatisticsScraperIT {

    @Value("${bedetheque.url.home}")
    private String bedethequeUrl;

    @Value("${application.downloads.localcache.active}")
    private boolean isLocalCacheActive;

    @Value("#{new Long('${application.scraping.latency}')}")
    private Long latency;

    @TempDir
    Path tempDir;

    @Test
    void scrapShouldReturnOk() {
        GlobalStatisticsScraper globalStatisticsScraper = new GlobalStatisticsScraper();
        globalStatisticsScraper.setBedethequeUrl(bedethequeUrl);
        globalStatisticsScraper.setLocalCacheActive(isLocalCacheActive);
        globalStatisticsScraper.setLatency(latency);

        String outputCoverFrontHdDirectory = tempDir.resolve("graphic-novels/cover-front/hd").toString();
        String outputCoverFrontThumbnailDirectory = tempDir.resolve("graphic-novels/cover-front/thb").toString();

        ReflectionTestUtils.setField(globalStatisticsScraper, "httpCoverFrontHdDirectory", "http://localhost:8080/media/graphic-novels/cover-front/hd/");
        ReflectionTestUtils.setField(globalStatisticsScraper, "httpCoverFrontThumbDirectory", "http://localhost:8080/media/graphic-novels/cover-front/thb/");
        ReflectionTestUtils.setField(globalStatisticsScraper, "outputCoverFrontHdDirectory", outputCoverFrontHdDirectory);
        ReflectionTestUtils.setField(globalStatisticsScraper, "outputCoverFrontThumbDirectory", outputCoverFrontThumbnailDirectory);
        ReflectionTestUtils.setField(globalStatisticsScraper, "hashedDirectoryStep", 5000);

        var globalStatistics = globalStatisticsScraper.scrap();
        assertThat(globalStatistics.getSeries()).isPositive();
        assertThat(globalStatistics.getGraphicNovels()).isPositive();
        assertThat(globalStatistics.getAuthors()).isPositive();
        assertThat(globalStatistics.getReviews()).isPositive();
        assertThat(globalStatistics.getLastEntries()).isNotEmpty();
        assertThat(globalStatistics.getNews()).isNotEmpty();
    }
}