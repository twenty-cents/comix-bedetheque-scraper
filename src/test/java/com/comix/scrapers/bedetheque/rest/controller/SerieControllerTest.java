package com.comix.scrapers.bedetheque.rest.controller;

import com.comix.scrapers.bedetheque.exception.BusinessException;
import com.comix.scrapers.bedetheque.rest.v1.dto.SerieDetailsDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.SeriesByLetterResponseDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.SeriesResponseDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.SeriesUrlResponseDto;
import com.comix.scrapers.bedetheque.service.SerieService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.comix.scrapers.bedetheque.rest.controller.SerieController.SCRAP_SERIES_URLS_BY_LETTER;
import static com.comix.scrapers.bedetheque.rest.controller.SerieController.SCRAP_SERIES_URLS_INDEXES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SerieControllerTest {

    @Mock
    private SerieService serieService;

    @InjectMocks
    private SerieController serieController;

    //=========================================================================
    // Tests for scrapSeries
    //=========================================================================

    @Test
    @DisplayName("scrapSeries should call service to get indexes when action is SCRAP_SERIES_URLS_INDEXES")
    void scrapSeries_withIndexesAction_shouldCallServiceAndReturnOk() {
        // GIVEN
        SeriesByLetterResponseDto mockResponse = new SeriesByLetterResponseDto();
        when(serieService.scrapSeriesIndexes()).thenReturn(mockResponse);

        // WHEN
        ResponseEntity<SeriesResponseDto> response = serieController.scrapSeries(SCRAP_SERIES_URLS_INDEXES, null);

        // THEN
        verify(serieService, times(1)).scrapSeriesIndexes();
        verify(serieService, never()).scrapSeriesIndexedByLetter(anyString());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(mockResponse);
    }

    @Test
    @DisplayName("scrapSeries should call service to get series by letter when action is SCRAP_SERIES_URLS_BY_LETTER")
    void scrapSeries_withByLetterAction_shouldCallServiceAndReturnOk() {
        // GIVEN
        String letter = "T";
        SeriesUrlResponseDto mockResponse = new SeriesUrlResponseDto();
        when(serieService.scrapSeriesIndexedByLetter(letter)).thenReturn(mockResponse);

        // WHEN
        ResponseEntity<SeriesResponseDto> response = serieController.scrapSeries(SCRAP_SERIES_URLS_BY_LETTER, letter);

        // THEN
        verify(serieService, never()).scrapSeriesIndexes();
        verify(serieService, times(1)).scrapSeriesIndexedByLetter(letter);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(mockResponse);
    }

    @Test
    @DisplayName("scrapSeries should throw BusinessException for unsupported action")
    void scrapSeries_withUnsupportedAction_shouldThrowBusinessException() {
        // GIVEN
        String unsupportedAction = "UNKNOWN_ACTION";

        // WHEN & THEN
        assertThatThrownBy(() -> serieController.scrapSeries(unsupportedAction, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("codeMessage", "UNSUPPORTED_ACTION");

        verifyNoInteractions(serieService);
    }

    @Test
    @DisplayName("scrapSeries should throw BusinessException when letter is missing for SCRAP_SERIES_URLS_BY_LETTER action")
    void scrapSeries_withByLetterActionAndMissingLetter_shouldThrowBusinessException() {
        // WHEN & THEN
        assertThatThrownBy(() -> serieController.scrapSeries(SCRAP_SERIES_URLS_BY_LETTER, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("codeMessage", "LETTER_NOT_FOUND");

        verifyNoInteractions(serieService);
    }

    //=========================================================================
    // Tests for scrapSerie
    //=========================================================================

    @Test
    @DisplayName("scrapSerie should call service with URL and return serie details")
    void scrapSerie_withValidUrl_shouldCallServiceAndReturnOk() {
        // GIVEN
        String serieId = "42";
        String url = "https://serie.com/42";
        SerieDetailsDto mockDetails = new SerieDetailsDto();
        mockDetails.setTitle("The Adventures of 42");
        when(serieService.scrap(url)).thenReturn(mockDetails);

        // WHEN
        ResponseEntity<SerieDetailsDto> response = serieController.scrapSerie(serieId, url);

        // THEN
        verify(serieService, times(1)).scrap(url);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("The Adventures of 42");
    }

    @Test
    @DisplayName("scrapSerie should return OK with a null body when service finds nothing")
    void scrapSerie_whenServiceReturnsNull_shouldReturnOkWithNullBody() {
        // GIVEN
        String serieId = "404";
        String url = "https://serie.com/404";
        when(serieService.scrap(url)).thenReturn(null);

        // WHEN
        ResponseEntity<SerieDetailsDto> response = serieController.scrapSerie(serieId, url);

        // THEN
        verify(serieService, times(1)).scrap(url);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
    }
}