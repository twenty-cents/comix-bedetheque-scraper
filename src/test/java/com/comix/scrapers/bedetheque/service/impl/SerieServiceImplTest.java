package com.comix.scrapers.bedetheque.service.impl;

import com.comix.scrapers.bedetheque.client.model.serie.Serie;
import com.comix.scrapers.bedetheque.client.model.serie.SerieDetails;
import com.comix.scrapers.bedetheque.client.model.serie.SeriesByLetter;
import com.comix.scrapers.bedetheque.client.scraper.SerieScraper;
import com.comix.scrapers.bedetheque.rest.mapper.SerieMapper;
import com.comix.scrapers.bedetheque.service.OutboxMessageProducer;
import com.comix.scrapers.bedetheque.rest.v1.dto.SerieDetailsDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.SeriesByLetterResponseDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.SeriesUrlResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SerieServiceImplTest {

    @Mock
    private SerieScraper serieScraper;

    @Mock
    private OutboxMessageProducer outboxMessageProducer;

    // On utilise @Spy car le service instancie le mapper lui-même.
    // Cela nous permet d'injecter une VRAIE instance du mapper
    // tout en pouvant vérifier ses appels.
    @Spy
    private SerieMapper serieMapper = Mappers.getMapper(SerieMapper.class);

    @InjectMocks
    private SerieServiceImpl serieService;

    @Nested
    @DisplayName("scrapSeriesIndexes Tests")
    class ScrapSeriesIndexesTests {

        @Test
        @DisplayName("should return mapped indexes when scraper finds data")
        void shouldReturnMappedIndexes_whenScraperSucceeds() {
            // GIVEN
            List<SeriesByLetter> scraperResult = List.of(new SeriesByLetter("A", "http://a.com"));
            when(serieScraper.listAllSeriesIndexes()).thenReturn(scraperResult);

            // WHEN
            SeriesByLetterResponseDto result = serieService.scrapSeriesIndexes();

            // THEN
            verify(serieScraper, times(1)).listAllSeriesIndexes();

            assertThat(result).isNotNull();
            assertThat(result.getSeriesByLetter()).hasSize(1);
            assertThat(result.getSeriesByLetter().getFirst().getLetter()).isEqualTo("A");
        }

        @Test
        @DisplayName("should return empty response when scraper returns empty list")
        void shouldReturnEmptyResponse_whenScraperReturnsEmptyList() {
            // GIVEN
            when(serieScraper.listAllSeriesIndexes()).thenReturn(Collections.emptyList());

            // WHEN
            SeriesByLetterResponseDto result = serieService.scrapSeriesIndexes();

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getSeriesByLetter()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("should throw NullPointerException when scraper returns null")
        void shouldThrowNPE_whenScraperReturnsNull() {
            // GIVEN
            when(serieScraper.listAllSeriesIndexes()).thenReturn(null);

            // WHEN & THEN
            // Le code actuel lève une NPE à cause de .stream() sur un null.
            // Un service plus robuste retournerait une liste vide.
            assertThatThrownBy(() -> serieService.scrapSeriesIndexes())
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("scrapSeriesIndexedByLetter Tests")
    class ScrapSeriesIndexedByLetterTests {

        @Test
        @DisplayName("should return mapped series when scraper finds data")
        void shouldReturnMappedSeries_whenScraperSucceeds() {
            // GIVEN
            String letter = "L";
            List<Serie> scraperResult = List.of(new Serie());
            when(serieScraper.listByLetter(letter)).thenReturn(scraperResult);

            // WHEN
            SeriesUrlResponseDto result = serieService.scrapSeriesIndexedByLetter(letter);

            // THEN
            verify(serieScraper, times(1)).listByLetter(letter);

            assertThat(result).isNotNull();
            assertThat(result.getSeriesUrls()).hasSize(1);
        }

        @Test
        @DisplayName("should throw NullPointerException when scraper returns null")
        void shouldThrowNPE_whenScraperReturnsNull() {
            // GIVEN
            String letter = "X";
            when(serieScraper.listByLetter(letter)).thenReturn(null);

            // WHEN & THEN
            assertThatThrownBy(() -> serieService.scrapSeriesIndexedByLetter(letter))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("scrap (Serie Details) Tests")
    class ScrapSerieDetailsTests {

        @Test
        @DisplayName("should return mapped details when scraper finds data")
        void shouldReturnMappedDetails_whenScraperSucceeds() {
            // GIVEN
            String url = "http://serie.com/42";
            SerieDetails scraperResult = new SerieDetails();
            when(serieScraper.scrap(url)).thenReturn(scraperResult);

            // WHEN
            SerieDetailsDto result = serieService.scrap(url);

            // THEN
            verify(serieScraper, times(1)).scrap(url);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("should return null when scraper returns null")
        void shouldReturnNull_whenScraperReturnsNull() {
            // GIVEN
            String url = "http://serie.com/404";
            when(serieScraper.scrap(url)).thenReturn(null);

            // WHEN
            SerieDetailsDto result = serieService.scrap(url);

            // THEN
            verify(serieScraper, times(1)).scrap(url);

            assertThat(result).isNull();
        }
    }
}