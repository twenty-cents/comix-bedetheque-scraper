package com.comix.scrapers.bedetheque.service.impl;

import com.comix.scrapers.bedetheque.client.model.media.Media;
import com.comix.scrapers.bedetheque.client.model.media.MediaType;
import com.comix.scrapers.bedetheque.client.scraper.MediaScraper;
import com.comix.scrapers.bedetheque.rest.mapper.MediaMapper;
import com.comix.scrapers.bedetheque.rest.v1.dto.MediaDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.MediaTypeDto;
import com.comix.scrapers.bedetheque.service.MediaService;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

@Service
public class MediaServiceImpl implements MediaService {

    private final MediaScraper mediaScraper;

    private final MediaMapper mediaMapper = Mappers.getMapper(MediaMapper.class);

    public MediaServiceImpl(MediaScraper mediaScraper) {
        this.mediaScraper = mediaScraper;
    }

    @Override
    public MediaDto scrap(String id, MediaTypeDto type, String originalUrl) {
        MediaType mediaType = MediaType.fromValue(type.name());
        Media media = mediaScraper.scrap(id, mediaType, originalUrl);
        return mediaMapper.mediaToMediaDto(media);
    }

}
