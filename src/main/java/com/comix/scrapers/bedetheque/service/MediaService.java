package com.comix.scrapers.bedetheque.service;

import com.comix.scrapers.bedetheque.rest.v1.dto.MediaDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.MediaTypeDto;
import org.springframework.stereotype.Service;

@Service
public interface MediaService {

    MediaDto scrap(String id, MediaTypeDto type, String originalUrl);
}
