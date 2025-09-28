package com.comix.scrapers.bedetheque.service;

import com.comix.scrapers.bedetheque.rest.v1.dto.RatingDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RatingService {

    List<RatingDto> scrap(String url);
}
