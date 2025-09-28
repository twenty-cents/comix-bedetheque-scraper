package com.comix.scrapers.bedetheque.service.impl;

import com.comix.scrapers.bedetheque.client.model.rating.Rating;
import com.comix.scrapers.bedetheque.client.scraper.RatingScraper;
import com.comix.scrapers.bedetheque.rest.mapper.RatingMapper;
import com.comix.scrapers.bedetheque.rest.v1.dto.RatingDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RatingServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class RatingServiceImplTest {

    @Mock
    private RatingScraper ratingScraper;

    // On utilise @Spy car le service instancie le mapper lui-même.
    // Cela nous permet d'injecter une VRAIE instance du mapper
    // tout en pouvant vérifier ses appels.
    @Spy
    private RatingMapper ratingMapper = Mappers.getMapper(RatingMapper.class);

    @InjectMocks
    private RatingServiceImpl ratingService;

    @Test
    @DisplayName("scrap should return a mapped list when scraper finds data")
    void scrap_shouldReturnMappedList_whenScraperSucceeds() {
        // GIVEN
        String url = "http://test.com/ratings";
        List<Rating> scraperResult = List.of(
                new Rating(),
                new Rating()
        );
        when(ratingScraper.scrap(url)).thenReturn(scraperResult);

        // WHEN
        List<RatingDto> result = ratingService.scrap(url);

        // THEN
        // 1. Le scraper a été appelé
        verify(ratingScraper, times(1)).scrap(url);

         // 2. Le résultat est une liste de DTOs correctement mappée
        assertThat(result).isNotNull().hasSize(2);
    }

    @Test
    @DisplayName("scrap should return an empty list when scraper returns an empty list")
    void scrap_shouldReturnEmptyList_whenScraperReturnsEmpty() {
        // GIVEN
        String url = "http://test.com/ratings-empty";
        when(ratingScraper.scrap(url)).thenReturn(Collections.emptyList());

        // WHEN
        List<RatingDto> result = ratingService.scrap(url);

        // THEN
        verify(ratingScraper, times(1)).scrap(url);
        // Le mapper ne doit jamais être appelé si la liste est vide
        verify(ratingMapper, never()).ratingToRatingDto(any());

        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("scrap should throw NullPointerException when scraper returns null")
    void scrap_shouldThrowNPE_whenScraperReturnsNull() {
        // GIVEN
        String url = "http://test.com/ratings-error";
        // Le scraper retourne null, simulant une erreur de scraping
        when(ratingScraper.scrap(url)).thenReturn(null);

        // WHEN & THEN
        // L'implémentation actuelle lèvera une NPE car elle fait .stream() sur un null
        assertThatThrownBy(() -> ratingService.scrap(url))
                .isInstanceOf(NullPointerException.class);

        // On vérifie que le scraper a bien été appelé
        verify(ratingScraper, times(1)).scrap(url);
    }
}