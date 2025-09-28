package com.comix.scrapers.bedetheque.rest.controller;

import com.comix.scrapers.bedetheque.rest.v1.dto.ScrapAllRepublicationsResponseDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.ScrapGraphicNovelsResponseDto;
import com.comix.scrapers.bedetheque.service.GraphicNovelService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphicNovelControllerTest {

    @Mock
    private GraphicNovelService graphicNovelService;

    @InjectMocks
    private GraphicNovelController graphicNovelController;

    @Test
    @DisplayName("scrapGraphicNovels should call service and return OK with the result")
    void scrapGraphicNovels_shouldCallServiceAndReturnOk() {
        // GIVEN
        String url = "http://some.url/graphic-novels";
        ScrapGraphicNovelsResponseDto mockResponse = new ScrapGraphicNovelsResponseDto();
        when(graphicNovelService.scrap(url, null)).thenReturn(mockResponse);

        // WHEN
        ResponseEntity<ScrapGraphicNovelsResponseDto> response = graphicNovelController.scrapGraphicNovels(url, null);

        // THEN
        verify(graphicNovelService, times(1)).scrap(url, null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(mockResponse);
    }

    @Test
    @DisplayName("scrapGraphicNovels should return OK with null body when service returns null")
    void scrapGraphicNovels_whenServiceReturnsNull_shouldReturnOkWithNullBody() {
        // GIVEN
        String url = "http://some.url/graphic-novels";
        when(graphicNovelService.scrap(url, null)).thenReturn(null);

        // WHEN
        ResponseEntity<ScrapGraphicNovelsResponseDto> response = graphicNovelController.scrapGraphicNovels(url, null);

        // THEN
        verify(graphicNovelService, times(1)).scrap(url, null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @DisplayName("scrapAllRepublications should call service and return OK with the result")
    void scrapAllRepublications_shouldCallServiceAndReturnOk() {
        // GIVEN
        String id = "12345";
        String url = "http://some.url/republications/12345";
        ScrapAllRepublicationsResponseDto mockResponse = new ScrapAllRepublicationsResponseDto();
        when(graphicNovelService.scrapWithAllRepublications(url)).thenReturn(mockResponse);

        // WHEN
        ResponseEntity<ScrapAllRepublicationsResponseDto> response = graphicNovelController.scrapAllRepublications(id, url);

        // THEN
        // Verify that the service is called with the URL, as the ID is only for logging
        verify(graphicNovelService, times(1)).scrapWithAllRepublications(url);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(mockResponse);
    }

    @Test
    @DisplayName("scrapAllRepublications should return OK with null body when service returns null")
    void scrapAllRepublications_whenServiceReturnsNull_shouldReturnOkWithNullBody() {
        // GIVEN
        String id = "12345";
        String url = "http://some.url/republications/12345";
        when(graphicNovelService.scrapWithAllRepublications(url)).thenReturn(null);

        // WHEN
        ResponseEntity<ScrapAllRepublicationsResponseDto> response = graphicNovelController.scrapAllRepublications(id, url);

        // THEN
        verify(graphicNovelService, times(1)).scrapWithAllRepublications(url);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
    }
}