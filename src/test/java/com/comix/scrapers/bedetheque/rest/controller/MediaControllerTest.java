package com.comix.scrapers.bedetheque.rest.controller;

import com.comix.scrapers.bedetheque.rest.v1.dto.MediaDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.MediaTypeDto;
import com.comix.scrapers.bedetheque.service.MediaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaControllerTest {

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private MediaController mediaController;

    @Test
    void scrapMedia_shouldReturnMediaDto_whenCalled() {
        // Given
        String id = "123";
        // We mock the DTO/Enum to avoid dependency on specific constants
        MediaTypeDto type = mock(MediaTypeDto.class);
        String url = "http://example.com/image.jpg";
        MediaDto expectedMediaDto = mock(MediaDto.class);

        when(mediaService.scrap(id, type, url)).thenReturn(expectedMediaDto);

        // When
        ResponseEntity<MediaDto> response = mediaController.scrapMedia(id, type, url);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedMediaDto);
        verify(mediaService).scrap(id, type, url);
    }
}