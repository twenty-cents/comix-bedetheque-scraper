package com.comix.scrapers.bedetheque.rest.controller;

import com.comix.scrapers.bedetheque.rest.v1.api.GraphicNovelsApi;
import com.comix.scrapers.bedetheque.rest.v1.dto.ScrapAllRepublicationsResponseDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.ScrapGraphicNovelsResponseDto;
import com.comix.scrapers.bedetheque.service.GraphicNovelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GraphicNovelController implements V1Controller, GraphicNovelsApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphicNovelController.class);

    private final GraphicNovelService graphicNovelService;

    public GraphicNovelController(GraphicNovelService graphicNovelService) {
        this.graphicNovelService = graphicNovelService;
    }

    /**
     * Scrap all graphics novels
     *
     * @param url  The graphic novels url (required)
     * @param page The page number (optional)
     * @return The list of graphic novels scraped from the url
     */
    @Override
    public ResponseEntity<ScrapGraphicNovelsResponseDto> scrapGraphicNovels(String url, Integer page) {
        LOGGER.info("Bedetheque - Scrap all graphic novels from url {}, page {}", url, page);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(graphicNovelService.scrap(url, page)
                );
    }

    /**
     * Scrap all republications of a graphic novel
     *
     * @param id  The graphic novel id (required)
     * @param url The url of the graphic novel (required)
     * @return The list of graphic novels scraped from the url
     */
    @Override
    public ResponseEntity<ScrapAllRepublicationsResponseDto> scrapAllRepublications(String id, String url) {
        LOGGER.info("Bedetheque - Scrap all republications of a graphic novel {} from url {}", id, url);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(graphicNovelService.scrapWithAllRepublications(url)
                );
    }
}
