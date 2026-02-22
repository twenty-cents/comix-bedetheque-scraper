package com.comix.scrapers.bedetheque.rest.controller;

import com.comix.scrapers.bedetheque.rest.v1.api.MediasApi;
import com.comix.scrapers.bedetheque.rest.v1.dto.MediaDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.MediaTypeDto;
import com.comix.scrapers.bedetheque.service.MediaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class MediaController implements V1Controller, MediasApi {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @Override
    public ResponseEntity<MediaDto> scrapMedia(@PathVariable("id") String id, MediaTypeDto type, @RequestHeader(value = "url") String url) {
        log.info("Bedetheque - Scrap media data (id {} - type {}) from url {}", id, type.name(), url);
        return ResponseEntity.status(HttpStatus.OK).body(mediaService.scrap(id, type, url));
    }
}
