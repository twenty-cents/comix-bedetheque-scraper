package com.comix.scrapers.bedetheque.rest.controller;

import com.comix.scrapers.bedetheque.rest.v1.api.RatingsApi;
import com.comix.scrapers.bedetheque.rest.v1.dto.RatingDto;
import com.comix.scrapers.bedetheque.service.RatingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RatingController implements V1Controller, RatingsApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(RatingController.class);

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    /**
     * Scrap serie's ratings from <a href="https://www.bedetheque.com">...</a>
     *
     * @param url The url of the ratings page to scrap. (required)
     * @return scraped serie's ratings
     */
    @Override
    public ResponseEntity<List<RatingDto>> scrapRatings(String url) {
        LOGGER.info("Bedetheque - Scrap serie ratings data from url {}", url);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ratingService.scrap(url)
                );
    }
}
