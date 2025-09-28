package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.client.model.filter.*;
import com.comix.scrapers.bedetheque.client.model.serie.SerieLanguage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(initializers = ConfigDataApplicationContextInitializer.class)
class FilterGraphicNovelsScraperIT {

    @Value("${bedetheque.url.search.graphic-novels}")
    private String bedethequeGraphicNovelsSearchUrl;

    @Value("${bedetheque.url.search.categories}")
    private String bedethequeCategoriesSearchUrl;

    @Value("${bedetheque.url.search.series}")
    private String bedethequeSeriesSearchUrl;

    @Value("${bedetheque.url.search.publishers}")
    private String bedethequePublishersSearchUrl;

    @Value("${bedetheque.url.search.collections}")
    private String bedethequeCollectionsSearchUrl;

    @Value("${bedetheque.url.search.authors}")
    private String bedethequeAuthorsSearchUrl;

    @Value("${application.downloads.localcache.active}")
    private boolean isLocalCacheActive;

    @Value("#{new Long('${application.scraping.latency}')}")
    private Long latency;

    private FilterGraphicNovelsScraper filterGraphicNovelsScraper;

    @BeforeEach
    void setup() {
        filterGraphicNovelsScraper = new FilterGraphicNovelsScraper();
        filterGraphicNovelsScraper.setLocalCacheActive(isLocalCacheActive);
        filterGraphicNovelsScraper.setBedethequeCategoriesSearchUrl(bedethequeCategoriesSearchUrl);
        filterGraphicNovelsScraper.setBedethequeSeriesSearchUrl(bedethequeSeriesSearchUrl);
        filterGraphicNovelsScraper.setBedethequePublishersSearchUrl(bedethequePublishersSearchUrl);
        filterGraphicNovelsScraper.setBedethequeCollectionsSearchUrl(bedethequeCollectionsSearchUrl);
        filterGraphicNovelsScraper.setBedethequeAuthorsSearchUrl(bedethequeAuthorsSearchUrl);
        filterGraphicNovelsScraper.setBedethequeGraphicNovelsSearchUrl(bedethequeGraphicNovelsSearchUrl);
        filterGraphicNovelsScraper.setLatency(latency);
    }

    @Test
    @DisplayName("Bedetheque scraping : use the Graphic novel filter to get a filtered list of graphic novels")
    void filter() {
        // Given - Filters criteria
        GraphicNovelsFilters graphicNovelsFilters = new GraphicNovelsFilters();
        graphicNovelsFilters.setSerieId("2");
        graphicNovelsFilters.setSerieTitle("Légendes des contrées oubliées");
        graphicNovelsFilters.setCategory("Heroic Fantasy");
        graphicNovelsFilters.setStatus(SerieStatus.FINISHED);
        graphicNovelsFilters.setOrigin(SerieOrigin.EUROPE);
        graphicNovelsFilters.setLanguage(SerieLanguage.FRENCH);
        graphicNovelsFilters.setGraphicnovelTitle("La saison des cendres");
        graphicNovelsFilters.setPublisher("Delcourt");
        graphicNovelsFilters.setCollection("Conquistador");
        graphicNovelsFilters.setPublicationDateFrom("11/1987");
        graphicNovelsFilters.setPublicationDateTo("11/1987");
        graphicNovelsFilters.setIsbn("2-906187-11-9");
        graphicNovelsFilters.setOriginalEdition("0");
        graphicNovelsFilters.setQuotationMin("5");
        graphicNovelsFilters.setQuotationMax("200");
        graphicNovelsFilters.setAuthorId("27");
        graphicNovelsFilters.setAuthor("Chevalier, Bruno");

        try {
            // Then - Should find only one graphic novel with these criteria
            GraphicNovelsFilteredObject graphicNovelsFilteredObject = filterGraphicNovelsScraper.filter(graphicNovelsFilters);

            assertThat(graphicNovelsFilteredObject.getFilteredGraphicNovelDetails())
                    .as("Get graphic novels by multiple filter criterion : \n" + graphicNovelsFilters + "\n")
                    .withFailMessage("No graphic novel found with these filter.")
                    .hasSize(1);

            assertThat(graphicNovelsFilteredObject.getFilteredGraphicNovelDetails().getFirst().getSerieTitle())
                    .isEqualTo("Légendes des Contrées Oubliées");
        } catch (IllegalArgumentException e1) {
            fail(e1.getMessage());
        }
    }

    @Test
    @DisplayName("Bedetheque scraping : use the graphic novel autocomplete to get a list of serie titles")
    void autocompleteSeries() {
        List<AutocompleteSearch> autocompleteSearches = filterGraphicNovelsScraper.autocompleteSeries("Légendes des contrées oubliées");
        // Test
        assertThat(autocompleteSearches).isNotEmpty();
        AutocompleteSearch autocomplete = autocompleteSearches.getFirst();
        assertThat(autocomplete.getLabel()).isEqualTo("Légendes des Contrées Oubliées");
    }

    @Test
    @DisplayName("Bedetheque scraping : use the graphic novel autocomplete to get a list of publishers")
    void autocompletePublishers() {
        List<AutocompleteSearch> autocompleteSearches = filterGraphicNovelsScraper.autocompletePublishers("Delcourt");
        // Test
        assertThat(autocompleteSearches).isNotEmpty();
        AutocompleteSearch autocomplete = autocompleteSearches.getFirst();
        assertThat(autocomplete.getLabel()).isEqualTo("Delcourt");
    }

    @Test
    @DisplayName("Bedetheque scraping : use the graphic novel autocomplete to get a list of collections")
    void autocompleteCollections() {
        List<AutocompleteSearch> autocompleteSearches = filterGraphicNovelsScraper.autocompleteCollections("Conquistador");
        // Test
        assertThat(autocompleteSearches).isNotEmpty();
        AutocompleteSearch autocomplete = autocompleteSearches.getFirst();
        assertThat(autocomplete.getLabel()).isEqualTo("Conquistador");
    }

    @Test
    @DisplayName("Bedetheque scraping : use the graphic novel autocomplete to get a list of categories")
    void autocompleteCategories() {
        List<AutocompleteSearch> autocompleteSearches = filterGraphicNovelsScraper.autocompleteCategories("Humour noir, Roman graphique");
        // Test
        assertThat(autocompleteSearches).isNotEmpty();
        AutocompleteSearch autocomplete = autocompleteSearches.getFirst();
        assertThat(autocomplete.getLabel()).isEqualTo("Humour noir, Roman graphique");
    }

    @Test
    @DisplayName("Bedetheque scraping : use the graphic novel autocomplete to get a list of authors")
    void autocompleteAuthors() {
        List<AutocompleteSearch> autocompleteSearches = filterGraphicNovelsScraper.autocompleteAuthors("Franquin, Isabelle");
        // Test
        assertThat(autocompleteSearches).isNotEmpty();
        AutocompleteSearch autocomplete = autocompleteSearches.getFirst();
        assertThat(autocomplete.getId()).isEqualTo("34832");
    }
}