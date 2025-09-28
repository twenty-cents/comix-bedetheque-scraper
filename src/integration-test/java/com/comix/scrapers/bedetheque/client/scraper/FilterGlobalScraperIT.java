package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.client.model.filter.GlobalFilteredObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(initializers = ConfigDataApplicationContextInitializer.class)
class FilterGlobalScraperIT {

    @Value("${bedetheque.url.search.global}")
    private String bedethequeGlobalSearchUrl;

    @Value("${application.downloads.localcache.active}")
    private boolean isLocalCacheActive;

    @Value("#{new Long('${application.scraping.latency}')}")
    private Long latency;

    private FilterGlobalScraper filterGlobalScraper;

    @BeforeEach
    void setup() {
        filterGlobalScraper = new FilterGlobalScraper();
        filterGlobalScraper.setLocalCacheActive(isLocalCacheActive);
        filterGlobalScraper.setBedethequeGlobalSearchUrl(bedethequeGlobalSearchUrl);
        filterGlobalScraper.setLatency(latency);
    }

    @Test
    @DisplayName("Bedetheque scraping : use the global filter to get some filtered lists about a search")
    void filter() {
        // Global search
        GlobalFilteredObject globalFilteredObject = filterGlobalScraper.filter("gaston");
        // Tests
        assertThat(globalFilteredObject).extracting(
                GlobalFilteredObject::getFilteredNews,
                GlobalFilteredObject::getFilteredChronicles,
                GlobalFilteredObject::getFilteredPreviews,
                GlobalFilteredObject::getFilteredAuthors,
                GlobalFilteredObject::getFilteredSeries,
                GlobalFilteredObject::getFilteredAssociateSeries
        ).hasSizeGreaterThan(0);
    }

}