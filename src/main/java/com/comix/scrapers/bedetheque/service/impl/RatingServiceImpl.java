package com.comix.scrapers.bedetheque.service.impl;

import com.comix.scrapers.bedetheque.client.scraper.RatingScraper;
import com.comix.scrapers.bedetheque.rest.mapper.RatingMapper;
import com.comix.scrapers.bedetheque.rest.v1.dto.RatingDto;
import com.comix.scrapers.bedetheque.service.RatingService;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RatingServiceImpl implements RatingService {

    private final RatingScraper ratingScraper;

    private final RatingMapper ratingMapper = Mappers.getMapper(RatingMapper.class);

    public RatingServiceImpl(RatingScraper ratingScraper) {
        this.ratingScraper = ratingScraper;
    }

    @Override
    public List<RatingDto> scrap(String url) {
        return ratingScraper.scrap(url)
                .stream()
                .map(ratingMapper::ratingToRatingDto)
                .toList();
    }
}

