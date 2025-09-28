package com.comix.scrapers.bedetheque.service;

import com.comix.scrapers.bedetheque.rest.v1.dto.SerieDetailsDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.SeriesByLetterResponseDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.SeriesResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface SerieService {

    /**
     * Scrap all series indexes
     *
     * @return The list of series indexes
     */
    SeriesByLetterResponseDto scrapSeriesIndexes();

    /**
     * Scrap all series indexed by letter
     *
     * @param letter The letter to filter series
     * @return The list of series indexed by letter
     */
    SeriesResponseDto scrapSeriesIndexedByLetter(String letter);

    /**
     * Scrap a serie by its url
     *
     * @param url The serie url
     * @return The serie details
     */
    SerieDetailsDto scrap(String url);
}
