package com.comix.scrapers.bedetheque.service;

import com.comix.scrapers.bedetheque.rest.v1.dto.ScrapAllRepublicationsResponseDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.ScrapGraphicNovelsResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface GraphicNovelService {

    /**
     * Scrap all graphics novels
     *
     * @param serieUrl The graphic novels url
     * @param page     The page number (optional)
     * @return The list of graphic novels scraped from the url
     */
    ScrapGraphicNovelsResponseDto scrap(String serieUrl, Integer page);

    /**
     * Scrap all republications of a graphic novel
     *
     * @param graphicNovelUrl The url of the graphic novel
     * @return The list of graphic novels scraped from the url
     */
    ScrapAllRepublicationsResponseDto scrapWithAllRepublications(String graphicNovelUrl);
}
