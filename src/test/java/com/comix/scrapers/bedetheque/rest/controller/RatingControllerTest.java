package com.comix.scrapers.bedetheque.rest.controller;

import com.comix.scrapers.bedetheque.rest.v1.dto.RatingDto;
import com.comix.scrapers.bedetheque.service.RatingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingControllerTest {

    @Mock
    private RatingService ratingService;

    @InjectMocks
    private RatingController ratingController;

    @Test
    @DisplayName("scrapRatings should call service and return OK with a list of ratings")
    void scrapRatings_shouldCallServiceAndReturnOkWithData() {
        // GIVEN
        String url = "https://www.bedetheque.com/serie-123-BD-Ratings.html";
        List<RatingDto> mockRatings = List.of(new RatingDto(), new RatingDto());
        when(ratingService.scrap(url)).thenReturn(mockRatings);

        // WHEN
        ResponseEntity<List<RatingDto>> response = ratingController.scrapRatings(url);

        // THEN
        // Verify the service was called with the correct URL
        verify(ratingService, times(1)).scrap(url);

        // Verify the response is correct
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).isEqualTo(mockRatings);
    }

    @Test
    @DisplayName("scrapRatings should return OK with an empty list when service finds nothing")
    void scrapRatings_whenServiceReturnsEmptyList_shouldReturnOkWithEmptyList() {
        // GIVEN
        String url = "https://www.bedetheque.com/serie-404-BD-Ratings.html";
        when(ratingService.scrap(url)).thenReturn(Collections.emptyList());

        // WHEN
        ResponseEntity<List<RatingDto>> response = ratingController.scrapRatings(url);

        // THEN
        verify(ratingService, times(1)).scrap(url);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("scrapRatings should return OK with a null body when service returns null")
    void scrapRatings_whenServiceReturnsNull_shouldReturnOkWithNullBody() {
        // GIVEN
        String url = "https://www.bedetheque.com/serie-500-BD-Ratings.html";
        when(ratingService.scrap(url)).thenReturn(null);

        // WHEN
        ResponseEntity<List<RatingDto>> response = ratingController.scrapRatings(url);

        // THEN
        verify(ratingService, times(1)).scrap(url);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
    }
}