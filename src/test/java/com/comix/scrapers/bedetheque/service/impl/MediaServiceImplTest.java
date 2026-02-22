package com.comix.scrapers.bedetheque.service.impl;

import com.comix.scrapers.bedetheque.client.model.media.Media;
import com.comix.scrapers.bedetheque.client.model.media.MediaType;
import com.comix.scrapers.bedetheque.client.scraper.MediaScraper;
import com.comix.scrapers.bedetheque.rest.mapper.MediaMapper;
import com.comix.scrapers.bedetheque.rest.v1.dto.MediaDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.MediaTypeDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaServiceImplTest {

    @Mock
    private MediaScraper mediaScraper;

    @Mock
    private MediaMapper mediaMapper;

    @InjectMocks
    private MediaServiceImpl mediaService;

    @BeforeEach
    void setUp() {
        // We inject the mock mapper because the field is initialized with Mappers.getMapper()
        // and we want to isolate the test from the MapStruct implementation.
        ReflectionTestUtils.setField(mediaService, "mediaMapper", mediaMapper);
    }

    @Test
    void scrap_shouldCallScraperAndMapper() {
        // Given
        String id = "123";
        String url = "http://example.com/image.jpg";
        MediaTypeDto typeDto = MediaTypeDto.COMIC_BOOK_COVER;

        Media scrapedMedia = new Media();
        scrapedMedia.setId(id);

        MediaDto expectedDto = new MediaDto();
        expectedDto.setId(id);

        when(mediaScraper.scrap(eq(id), any(MediaType.class), eq(url))).thenReturn(scrapedMedia);
        when(mediaMapper.mediaToMediaDto(scrapedMedia)).thenReturn(expectedDto);

        // When
        MediaDto result = mediaService.scrap(id, typeDto, url);

        // Then
        assertThat(result).isSameAs(expectedDto);
        verify(mediaScraper).scrap(eq(id), any(MediaType.class), eq(url));
        verify(mediaMapper).mediaToMediaDto(scrapedMedia);
    }
}