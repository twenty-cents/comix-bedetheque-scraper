package com.comix.scrapers.bedetheque.rest.controller;

import com.comix.scrapers.bedetheque.client.model.filter.GraphicNovelsFilters;
import com.comix.scrapers.bedetheque.client.model.filter.SerieOrigin;
import com.comix.scrapers.bedetheque.client.model.filter.SerieStatus;
import com.comix.scrapers.bedetheque.client.model.serie.SerieLanguage;
import com.comix.scrapers.bedetheque.rest.v1.dto.AutocompleteSearchDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.FilterAutocompleteTypeEnumDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.GlobalFilteredDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.GraphicNovelsFilteredDto;
import com.comix.scrapers.bedetheque.service.FilterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilterControllerTest {

    @Mock
    private FilterService filterService;

    @InjectMocks
    private FilterController filterController;

    @Test
    @DisplayName("globalSearch should call service and return OK with result")
    void globalSearch_shouldCallServiceAndReturnOk() {
        // GIVEN
        String filter = "test-filter";
        GlobalFilteredDto mockResponse = new GlobalFilteredDto();
        when(filterService.globalSearch(filter)).thenReturn(mockResponse);

        // WHEN
        ResponseEntity<GlobalFilteredDto> response = filterController.globalSearch(filter);

        // THEN
        verify(filterService, times(1)).globalSearch(filter);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(mockResponse);
    }

    @Nested
    @DisplayName("graphicNovelSearch Tests")
    class GraphicNovelSearchTests {

        @Test
        @DisplayName("should call service with correctly mapped filters")
        void graphicNovelSearch_shouldMapAndCallService() {
            // GIVEN
            GraphicNovelsFilteredDto mockResponse = new GraphicNovelsFilteredDto();
            when(filterService.graphicNovelSearch(any(GraphicNovelsFilters.class))).thenReturn(mockResponse);

            String serieTitle = "Lanfeust";
            String status = "ONE_SHOT";
            String origin = "USA";
            String language = "VF";

            // WHEN
            ResponseEntity<GraphicNovelsFilteredDto> response = filterController.graphicNovelSearch(
                    null, null, serieTitle, null, null, null, null, null, null,
                    status, origin, language, null, null, null, null, null, null
            );

            // THEN
            ArgumentCaptor<GraphicNovelsFilters> captor = ArgumentCaptor.forClass(GraphicNovelsFilters.class);
            verify(filterService, times(1)).graphicNovelSearch(captor.capture());

            GraphicNovelsFilters capturedFilters = captor.getValue();
            assertThat(capturedFilters.getSerieTitle()).isEqualTo(serieTitle);
            assertThat(capturedFilters.getStatus()).isEqualTo(SerieStatus.fromValue(status));
            assertThat(capturedFilters.getOrigin()).isEqualTo(SerieOrigin.fromValue(origin));
            assertThat(capturedFilters.getLanguage()).isEqualTo(SerieLanguage.fromValue(language));
            assertThat(capturedFilters.getAuthorId()).isEmpty();
            assertThat(capturedFilters.getIsbn()).isEmpty();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(mockResponse);
        }

        @Test
        @DisplayName("should handle all null parameters gracefully")
        void graphicNovelSearch_withAllNulls_shouldCallServiceWithEmptyFilters() {
            // GIVEN
            when(filterService.graphicNovelSearch(any(GraphicNovelsFilters.class))).thenReturn(new GraphicNovelsFilteredDto());

            // WHEN
            filterController.graphicNovelSearch(
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null
            );

            // THEN
            ArgumentCaptor<GraphicNovelsFilters> captor = ArgumentCaptor.forClass(GraphicNovelsFilters.class);
            verify(filterService, times(1)).graphicNovelSearch(captor.capture());

            GraphicNovelsFilters capturedFilters = captor.getValue();
            assertThat(capturedFilters.getSerieTitle()).isEmpty();
            assertThat(capturedFilters.getStatus().name()).isEqualTo(SerieStatus.UNAVAILABLE.name());
            assertThat(capturedFilters.getOrigin().name()).isEqualTo(SerieOrigin.UNAVAILABLE.name());
            assertThat(capturedFilters.getLanguage().name()).isEqualTo(SerieLanguage.UNAVAILABLE.name());
        }
    }

    @Nested
    @DisplayName("autocomplete Tests")
    class AutocompleteTests {

        @Test
        @DisplayName("should call autocompleteSeries for SERIES type")
        void autocomplete_withSeriesType_shouldCallCorrectServiceMethod() {
            // GIVEN
            String filter = "tintin";
            List<AutocompleteSearchDto> mockResponse = List.of(new AutocompleteSearchDto());
            when(filterService.autocompleteSeries(filter)).thenReturn(mockResponse);

            // WHEN
            ResponseEntity<List<AutocompleteSearchDto>> response = filterController.autocomplete(FilterAutocompleteTypeEnumDto.SERIES, filter);

            // THEN
            verify(filterService, times(1)).autocompleteSeries(filter);
            verifyNoMoreInteractions(filterService); // Ensure no other autocomplete methods are called
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(mockResponse);
        }

        @Test
        @DisplayName("should call autocompletePublishers for PUBLISHERS type")
        void autocomplete_withPublishersType_shouldCallCorrectServiceMethod() {
            // GIVEN
            String filter = "dargaud";
            when(filterService.autocompletePublishers(filter)).thenReturn(Collections.emptyList());

            // WHEN
            filterController.autocomplete(FilterAutocompleteTypeEnumDto.PUBLISHERS, filter);

            // THEN
            verify(filterService, times(1)).autocompletePublishers(filter);
            verifyNoMoreInteractions(filterService);
        }

        @Test
        @DisplayName("should call autocompleteCollections for COLLECTIONS type")
        void autocomplete_withCollectionsType_shouldCallCorrectServiceMethod() {
            // GIVEN
            String filter = "troisième";
            when(filterService.autocompleteCollections(filter)).thenReturn(Collections.emptyList());

            // WHEN
            filterController.autocomplete(FilterAutocompleteTypeEnumDto.COLLECTIONS, filter);

            // THEN
            verify(filterService, times(1)).autocompleteCollections(filter);
            verifyNoMoreInteractions(filterService);
        }

        @Test
        @DisplayName("should call autocompleteAuthors for AUTHORS type")
        void autocomplete_withAuthorsType_shouldCallCorrectServiceMethod() {
            // GIVEN
            String filter = "goscinny";
            when(filterService.autocompleteAuthors(filter)).thenReturn(Collections.emptyList());

            // WHEN
            filterController.autocomplete(FilterAutocompleteTypeEnumDto.AUTHORS, filter);

            // THEN
            verify(filterService, times(1)).autocompleteAuthors(filter);
            verifyNoMoreInteractions(filterService);
        }

        @Test
        @DisplayName("should call autocompleteCategories for CATEGORIES type")
        void autocomplete_withCategoriesType_shouldCallCorrectServiceMethod() {
            // GIVEN
            String filter = "humour";
            when(filterService.autocompleteCategories(filter)).thenReturn(Collections.emptyList());

            // WHEN
            filterController.autocomplete(FilterAutocompleteTypeEnumDto.CATEGORIES, filter);

            // THEN
            verify(filterService, times(1)).autocompleteCategories(filter);
            verifyNoMoreInteractions(filterService);
        }
    }
}