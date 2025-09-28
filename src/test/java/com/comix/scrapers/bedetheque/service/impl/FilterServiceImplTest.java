package com.comix.scrapers.bedetheque.service.impl;

import com.comix.scrapers.bedetheque.client.model.filter.AutocompleteSearch;
import com.comix.scrapers.bedetheque.client.model.filter.GlobalFilteredObject;
import com.comix.scrapers.bedetheque.client.model.filter.GraphicNovelsFilteredObject;
import com.comix.scrapers.bedetheque.client.model.filter.GraphicNovelsFilters;
import com.comix.scrapers.bedetheque.client.scraper.FilterGlobalScraper;
import com.comix.scrapers.bedetheque.client.scraper.FilterGraphicNovelsScraper;
import com.comix.scrapers.bedetheque.rest.mapper.FilterMapper;
import com.comix.scrapers.bedetheque.rest.v1.dto.AutocompleteSearchDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.GlobalFilteredDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.GraphicNovelsFilteredDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilterServiceImplTest {

    @Mock
    private FilterGlobalScraper filterGlobalScraper;

    @Mock
    private FilterGraphicNovelsScraper filterGraphicNovelsScraper;

    // On utilise @Spy pour injecter une vraie instance du mapper,
    // car il est instancié directement dans le service.
    // Cela nous permet de tester le résultat du mapping sans mocker le mapper lui-même.
    @Spy
    private FilterMapper filterMapper = Mappers.getMapper(FilterMapper.class);

    @InjectMocks
    private FilterServiceImpl filterService;

    @Nested
    @DisplayName("Autocomplete Tests")
    class AutocompleteTests {

        @Test
        @DisplayName("autocompleteAuthors should call scraper and map results")
        void autocompleteAuthors_shouldCallScraperAndMapResults() {
            // GIVEN
            String filter = "Uderzo";
            List<AutocompleteSearch> scraperResult = List.of(new AutocompleteSearch("id", "label", "value", "desc"));
            when(filterGraphicNovelsScraper.autocompleteAuthors(filter)).thenReturn(scraperResult);

            // WHEN
            List<AutocompleteSearchDto> result = filterService.autocompleteAuthors(filter);

            // THEN
            verify(filterGraphicNovelsScraper, times(1)).autocompleteAuthors(filter);
            assertThat(result).isNotNull().hasSize(1);
            assertThat(result.getFirst().getLabel()).isEqualTo("label");
        }

        @Test
        @DisplayName("autocompleteCategories should call scraper and map results")
        void autocompleteCategories_shouldCallScraperAndMapResults() {
            // GIVEN
            String filter = "Aventure";
            List<AutocompleteSearch> scraperResult = List.of(new AutocompleteSearch("id", "label", filter, "desc"));
            when(filterGraphicNovelsScraper.autocompleteCategories(filter)).thenReturn(scraperResult);

            // WHEN
            List<AutocompleteSearchDto> result = filterService.autocompleteCategories(filter);

            // THEN
            verify(filterGraphicNovelsScraper, times(1)).autocompleteCategories(filter);
            assertThat(result).isNotNull().hasSize(1);
            assertThat(result.getFirst().getValue()).isEqualTo(filter);
        }

        @Test
        @DisplayName("autocompleteCollections should call scraper and map results")
        void autocompleteCollections_shouldCallScraperAndMapResults() {
            // GIVEN
            String filter = "Aire Libre";
            List<AutocompleteSearch> scraperResult = List.of(new AutocompleteSearch("id", "label", filter, "desc"));
            when(filterGraphicNovelsScraper.autocompleteCollections(filter)).thenReturn(scraperResult);

            // WHEN
            List<AutocompleteSearchDto> result = filterService.autocompleteCollections(filter);

            // THEN
            verify(filterGraphicNovelsScraper, times(1)).autocompleteCollections(filter);
            assertThat(result).isNotNull().hasSize(1);
            assertThat(result.getFirst().getValue()).isEqualTo(filter);
        }

        @Test
        @DisplayName("autocompletePublishers should call scraper and map results")
        void autocompletePublishers_shouldCallScraperAndMapResults() {
            // GIVEN
            String filter = "Dupuis";
            List<AutocompleteSearch> scraperResult = List.of(new AutocompleteSearch("id", "label", filter, "desc"));
            when(filterGraphicNovelsScraper.autocompletePublishers(filter)).thenReturn(scraperResult);

            // WHEN
            List<AutocompleteSearchDto> result = filterService.autocompletePublishers(filter);

            // THEN
            verify(filterGraphicNovelsScraper, times(1)).autocompletePublishers(filter);
            assertThat(result).isNotNull().hasSize(1);
            assertThat(result.getFirst().getValue()).isEqualTo(filter);
        }

        @Test
        @DisplayName("autocompleteSeries should call scraper and map results")
        void autocompleteSeries_shouldCallScraperAndMapResults() {
            // GIVEN
            String filter = "Spirou";
            List<AutocompleteSearch> scraperResult = List.of(new AutocompleteSearch("id", "label", filter, "desc"));
            when(filterGraphicNovelsScraper.autocompleteSeries(filter)).thenReturn(scraperResult);

            // WHEN
            List<AutocompleteSearchDto> result = filterService.autocompleteSeries(filter);

            // THEN
            verify(filterGraphicNovelsScraper, times(1)).autocompleteSeries(filter);
            assertThat(result).isNotNull().hasSize(1);
            assertThat(result.getFirst().getValue()).isEqualTo(filter);
        }

        @Test
        @DisplayName("autocomplete should return empty list when scraper returns empty list")
        void autocomplete_shouldReturnEmptyList_whenScraperReturnsEmpty() {
            // GIVEN
            String filter = "nonexistent";
            when(filterGraphicNovelsScraper.autocompleteAuthors(filter)).thenReturn(Collections.emptyList());

            // WHEN
            List<AutocompleteSearchDto> result = filterService.autocompleteAuthors(filter);

            // THEN
            assertThat(result).isNotNull().isEmpty();
        }
    }

    @Nested
    @DisplayName("Search Tests")
    class SearchTests {

        @Test
        @DisplayName("globalSearch should call scraper and map result")
        void globalSearch_shouldCallScraperAndMapResult() {
            // GIVEN
            String filter = "test";
            GlobalFilteredObject scraperResult = new GlobalFilteredObject();
            when(filterGlobalScraper.filter(filter)).thenReturn(scraperResult);

            // WHEN
            GlobalFilteredDto result = filterService.globalSearch(filter);

            // THEN
            verify(filterGlobalScraper, times(1)).filter(filter);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("globalSearch should return null when scraper returns null")
        void globalSearch_shouldReturnNull_whenScraperReturnsNull() {
            // GIVEN
            String filter = "test";
            when(filterGlobalScraper.filter(filter)).thenReturn(null);

            // WHEN
            GlobalFilteredDto result = filterService.globalSearch(filter);

            // THEN
            verify(filterGlobalScraper, times(1)).filter(filter);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("graphicNovelSearch should call scraper and map result")
        void graphicNovelSearch_shouldCallScraperAndMapResult() {
            // GIVEN
            GraphicNovelsFilters filters = new GraphicNovelsFilters();
            GraphicNovelsFilteredObject scraperResult = new GraphicNovelsFilteredObject();
            when(filterGraphicNovelsScraper.filter(filters)).thenReturn(scraperResult);

            // WHEN
            GraphicNovelsFilteredDto result = filterService.graphicNovelSearch(filters);

            // THEN
            verify(filterGraphicNovelsScraper, times(1)).filter(filters);
            assertThat(result).isNotNull();
        }
    }
}