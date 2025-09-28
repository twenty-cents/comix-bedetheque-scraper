package com.comix.scrapers.bedetheque.rest.controller;

import com.comix.scrapers.bedetheque.exception.BusinessException;
import com.comix.scrapers.bedetheque.rest.v1.api.SeriesApi;
import com.comix.scrapers.bedetheque.rest.v1.dto.SerieDetailsDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.SeriesResponseDto;
import com.comix.scrapers.bedetheque.service.SerieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SerieController implements V1Controller, SeriesApi {

    public static final String SCRAP_SERIES_URLS_INDEXES = "SCRAP_SERIES_URLS_INDEXES";
    public static final String SCRAP_SERIES_URLS_BY_LETTER = "SCRAP_SERIES_URLS_BY_LETTER";

    private static final Logger LOGGER = LoggerFactory.getLogger(SerieController.class);

    private final SerieService serieService;

    public SerieController(SerieService serieService) {
        this.serieService = serieService;
    }

    /**
     * Scrap series data
     *
     * @param action The action to execute.
     *               - SCRAP_SERIES_URLS_INDEXES : Scrap alphabetical indexes urls of all series.
     *               - SCRAP_SERIES_URLS_BY_LETTER : Scrap the list of all series starting with a given letter. (required)
     * @param letter The letter to filter series' urls.  Mandatory if the action is SCRAP_SERIES_URLS_BY_LETTER. (optional)
     * @return scraped authors data
     */
    @Override
    public ResponseEntity<SeriesResponseDto> scrapSeries(String action, String letter) {
        // Check if the action is available
        if (!(action.equals(SCRAP_SERIES_URLS_BY_LETTER) || action.equals(SCRAP_SERIES_URLS_INDEXES))) {
            throw new BusinessException("UNSUPPORTED_ACTION", new Object[]{action});
        }

        // Check if a letter is provided if the action is scrap the list of all series starting with a given letter
        if (action.equals(SCRAP_SERIES_URLS_BY_LETTER) && letter == null) {
            throw new BusinessException("LETTER_NOT_FOUND", new Object[]{action});
        }

        return switch (action) {
            case SCRAP_SERIES_URLS_INDEXES -> {
                LOGGER.info("Bedetheque - Scrap all series indexes");
                yield ResponseEntity
                        .status(HttpStatus.OK)
                        .body(serieService.scrapSeriesIndexes());
            }
            case SCRAP_SERIES_URLS_BY_LETTER -> {
                LOGGER.info("Bedetheque - Scrap all series starting with letter {}", letter);
                yield ResponseEntity
                        .status(HttpStatus.OK)
                        .body(serieService.scrapSeriesIndexedByLetter(letter));
            }
            default -> throw new BusinessException("UNSUPPORTED_ACTION", new Object[]{action});
        };
    }

    /**
     * Scrap a serie
     *
     * @param id  the serie id (required)
     * @param url The url of the serie to scrap. (required)
     * @return the serie details
     */
    @Override
    public ResponseEntity<SerieDetailsDto> scrapSerie(String id, String url) {
        LOGGER.info("Bedetheque - Scrap serie data (id {}) from url {}", id, url);
        return ResponseEntity.status(HttpStatus.OK).body(
                serieService.scrap(url)
        );
    }
}
