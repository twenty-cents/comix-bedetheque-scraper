package com.comix.scrapers.bedetheque.service.impl;

import com.comix.scrapers.bedetheque.client.model.graphicnovel.GraphicNovel;
import com.comix.scrapers.bedetheque.client.model.graphicnovel.GraphicNovelPage;
import com.comix.scrapers.bedetheque.client.scraper.GraphicNovelScraper;
import com.comix.scrapers.bedetheque.rest.mapper.GraphicNovelMapper;
import com.comix.scrapers.bedetheque.service.OutboxMessageProducer;
import com.comix.scrapers.bedetheque.rest.v1.dto.ScrapAllRepublicationsResponseDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.ScrapGraphicNovelsResponseDto;
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
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GraphicNovelServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class GraphicNovelServiceImplTest {

    @Mock
    private GraphicNovelScraper graphicNovelScraper;

    @Mock
    private OutboxMessageProducer outboxMessageProducer;

    // On utilise @Spy pour injecter une VRAIE instance du mapper dans le service,
    // tout en nous permettant de vérifier ses appels avec Mockito.
    @Spy
    private GraphicNovelMapper graphicNovelMapper = Mappers.getMapper(GraphicNovelMapper.class);

    @InjectMocks
    private GraphicNovelServiceImpl graphicNovelService;

    @Nested
    @DisplayName("Tests for scrap(serieUrl, page)")
    class ScrapTests {

        @Test
        @DisplayName("doit appeler le scraper avec la page 10000 si la page est nulle")
        void scrap_shouldCallScraperWithDefaultPage_whenPageIsNull() {
            // GIVEN
            String serieUrl = "http://test.com/serie/1";
            GraphicNovelPage emptyPage = new GraphicNovelPage(0, 0, 0, 0, Collections.emptyList());
            when(graphicNovelScraper.scrapElement(serieUrl, 10000)).thenReturn(emptyPage);

            // WHEN
            graphicNovelService.scrap(serieUrl, null);

            // THEN
            // On vérifie que le scraper a été appelé avec la bonne URL et la page par défaut
            verify(graphicNovelScraper, times(1)).scrapElement(serieUrl, 10000);
        }

        @Test
        @DisplayName("doit retourner un DTO avec des données quand le scraper réussit")
        void scrap_shouldReturnDtoWithData_whenScraperSucceeds() {
            // GIVEN
            String serieUrl = "http://test.com/serie/1";
            int page = 2;
            List<GraphicNovel> scrapedNovels = List.of(new GraphicNovel());
            GraphicNovelPage graphicNovelPage = new GraphicNovelPage(page, 1, 1, 1, scrapedNovels);

            when(graphicNovelScraper.scrapElement(serieUrl, page)).thenReturn(graphicNovelPage);

            // WHEN
            ScrapGraphicNovelsResponseDto result = graphicNovelService.scrap(serieUrl, page);

            // THEN
            // On vérifie que le scraper et le mapper ont été appelés
            verify(graphicNovelScraper, times(1)).scrapElement(serieUrl, page);

            // On vérifie que le DTO final est correctement rempli
            assertThat(result).isNotNull();
            assertThat(result.getGraphicNovels()).isNotNull().hasSize(1);
            assertThat(result.getPage()).isEqualTo(page);
            assertThat(result.getSize()).isEqualTo(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getTotalPages()).isEqualTo(1);
        }

        @Test
        @DisplayName("doit retourner un DTO avec une liste vide quand le scraper ne trouve rien")
        void scrap_shouldReturnDtoWithEmptyList_whenScraperFindsNothing() {
            // GIVEN
            String serieUrl = "http://test.com/serie/empty";
            GraphicNovelPage emptyPage = new GraphicNovelPage(0, 0, 0, 0, Collections.emptyList());
            when(graphicNovelScraper.scrapElement(serieUrl, 10000)).thenReturn(emptyPage);

            // WHEN
            ScrapGraphicNovelsResponseDto result = graphicNovelService.scrap(serieUrl, null);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getGraphicNovels()).isNotNull().isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("Tests for scrapWithAllRepublications(graphicNovelUrl)")
    class ScrapWithAllRepublicationsTests {

        @Test
        @DisplayName("doit retourner un DTO avec des données quand le scraper réussit")
        void scrapWithAllRepublications_shouldReturnDtoWithData_whenScraperSucceeds() {
            // GIVEN
            String graphicNovelUrl = "http://test.com/album/1";
            List<GraphicNovel> scrapedRepublications = List.of(new GraphicNovel());
            when(graphicNovelScraper.scrapWithAllRepublications(graphicNovelUrl)).thenReturn(scrapedRepublications);

            // WHEN
            ScrapAllRepublicationsResponseDto result = graphicNovelService.scrapWithAllRepublications(graphicNovelUrl);

            // THEN
            verify(graphicNovelScraper, times(1)).scrapWithAllRepublications(graphicNovelUrl);

            assertThat(result).isNotNull();
            assertThat(result.getGraphicNovels()).isNotNull().hasSize(1);
        }

        @Test
        @DisplayName("doit retourner un DTO avec une liste vide quand le scraper ne trouve rien")
        void scrapWithAllRepublications_shouldReturnDtoWithEmptyList_whenScraperFindsNothing() {
            // GIVEN
            String graphicNovelUrl = "http://test.com/album/empty";
            when(graphicNovelScraper.scrapWithAllRepublications(graphicNovelUrl)).thenReturn(Collections.emptyList());

            // WHEN
            ScrapAllRepublicationsResponseDto result = graphicNovelService.scrapWithAllRepublications(graphicNovelUrl);

            // THEN
            verify(graphicNovelScraper, times(1)).scrapWithAllRepublications(graphicNovelUrl);

            assertThat(result).isNotNull();
            assertThat(result.getGraphicNovels()).isNotNull().isEmpty();
        }
    }
}