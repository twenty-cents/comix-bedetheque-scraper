package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.client.model.filter.*;
import com.comix.scrapers.bedetheque.client.model.serie.SerieLanguage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilterGraphicNovelsScraperTest {

    private FilterGraphicNovelsScraper scraper;

    @BeforeEach
    void setUp() {
        scraper = new FilterGraphicNovelsScraper();
        // Injection manuelle des dépendances @Value pour les tests
        scraper.setBedethequeGraphicNovelsSearchUrl("https://test.com/search/graphic-novels");
        scraper.setLatency(0L);
        scraper.setLocalCacheActive(false);
    }

    @Nested
    @DisplayName("Tests for filter(GraphicNovelsFilters)")
    class FilterTests {

        @Test
        @DisplayName("doit parser correctement une page avec des résultats")
        void filter_shouldParsePageWithResults() {
            // GIVEN: Un document HTML simulé avec une liste de résultats
            String html = """
                <div>
                    <div class="widget-line-title"><h3>Résultats de la recherche</h3></div>
                    <ul>
                        <li>
                            <span class="ico"><img src="flag1.png"></span>
                            <a href="https://test.com/album-1.html" rel="cover1.jpg"></a>
                            <span class="serie">Série Test 1</span>
                            <span class="num">T.1</span>
                            <span class="numa">(a)</span>
                            <span class="titre">Titre de l'album 1</span>
                            <span class="dl">01/2024</span>
                        </li>
                        <li>
                            <span class="ico"><img src="flag2.png"></span>
                            <a href="https://test.com/album-2.html" rel="cover2.jpg"></a>
                            <span class="serie">Série Test 2</span>
                            <span class="num">T.2</span>
                            <span class="numa"></span>
                            <span class="titre">Titre de l'album 2</span>
                            <span class="dl">02/2024</span>
                        </li>
                    </ul>
                </div>
            """;
            Document doc = Jsoup.parse(html);
            GraphicNovelsFilters filters = new GraphicNovelsFilters();

            try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class)) {
                GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
                mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);
                when(mockScraper.load(anyString(), anyMap(), anyLong())).thenReturn(doc);

                // WHEN
                GraphicNovelsFilteredObject result = scraper.filter(filters);

                // THEN
                assertThat(result).isNotNull();
                assertThat(result.getFilteredGraphicNovelsMessage()).isEmpty();
                assertThat(result.getFilteredGraphicNovelDetails()).hasSize(2);

                FilteredGraphicNovelDetails firstResult = result.getFilteredGraphicNovelDetails().getFirst();
                assertThat(firstResult.getFlagUrl()).isEqualTo("flag1.png");
                assertThat(firstResult.getUrl()).isEqualTo("https://test.com/album-1.html");
                assertThat(firstResult.getCoverUrl()).isEqualTo("cover1.jpg");
                assertThat(firstResult.getSerieTitle()).isEqualTo("Série Test 1");
                assertThat(firstResult.getTome()).isEqualTo("T.1");
                assertThat(firstResult.getNumEdition()).isEqualTo("(a)");
                assertThat(firstResult.getTitle()).isEqualTo("Titre de l'album 1");
                assertThat(firstResult.getPublicationDate()).isEqualTo("01/2024");
            }
        }

        @Test
        @DisplayName("doit gérer le message d'erreur 'Trop de résultats'")
        void filter_shouldHandleTooManyResultsError() {
            // GIVEN: Un document HTML avec un message d'erreur
            String html = """
                <div>
                    <div class="widget-line-title"><h3>Résultats</h3></div>
                    <span class="erreur"> Trop de résultats, veuillez affiner votre recherche. </span>
                </div>
            """;
            Document doc = Jsoup.parse(html);
            GraphicNovelsFilters filters = new GraphicNovelsFilters();

            try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class)) {
                GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
                mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);
                when(mockScraper.load(anyString(), anyMap(), anyLong())).thenReturn(doc);

                // WHEN
                GraphicNovelsFilteredObject result = scraper.filter(filters);

                // THEN
                assertThat(result).isNotNull();
                assertThat(result.getFilteredGraphicNovelDetails()).isNotNull().isEmpty();
                assertThat(result.getFilteredGraphicNovelsMessage()).isEqualTo("Trop de résultats, veuillez affiner votre recherche.");
            }
        }

        @Test
        @DisplayName("doit retourner un objet vide si aucun résultat n'est trouvé")
        void filter_shouldReturnEmptyObjectWhenNoResults() {
            // GIVEN: Un document HTML sans la section de résultats
            String html = "<html><body><h1>Aucun résultat</h1></body></html>";
            Document doc = Jsoup.parse(html);
            GraphicNovelsFilters filters = new GraphicNovelsFilters();

            try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class)) {
                GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
                mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);
                when(mockScraper.load(anyString(), anyMap(), anyLong())).thenReturn(doc);

                // WHEN
                GraphicNovelsFilteredObject result = scraper.filter(filters);

                // THEN
                assertThat(result).isNotNull();
                assertThat(result.getFilteredGraphicNovelDetails()).isNotNull().isEmpty();
                assertThat(result.getFilteredGraphicNovelsMessage()).isEmpty();
            }
        }

        @Test
        @DisplayName("doit passer tous les paramètres de filtre à la méthode de chargement")
        void filter_shouldPassAllFilterParametersToLoad() {
            // GIVEN: Un objet de filtre complet
            GraphicNovelsFilters filters = new GraphicNovelsFilters();
            filters.setSerieId("123");
            filters.setAuthorId("456");
            filters.setSerieTitle("Astérix");
            filters.setGraphicnovelTitle("Le bouclier Arverne");
            filters.setPublisher("Dargaud");
            filters.setCollection("La collection");
            filters.setCategory("Humour");
            filters.setAuthor("Goscinny");
            filters.setIsbn("978-2205001584");
            filters.setStatus(SerieStatus.UNAVAILABLE);
            filters.setOrigin(SerieOrigin.UNAVAILABLE);
            filters.setLanguage(SerieLanguage.UNAVAILABLE);
            filters.setKeyword("Gaulois");
            filters.setPublicationDateFrom("01/01/1960");
            filters.setPublicationDateTo("31/12/1970");
            filters.setQuotationMin("10");
            filters.setQuotationMax("100");
            filters.setOriginalEdition("on");

            Document doc = Jsoup.parse("<div></div>");
            ArgumentCaptor<Map<String, String>> dataCaptor = ArgumentCaptor.forClass(Map.class);

            try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class)) {
                GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
                mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);
                when(mockScraper.load(anyString(), dataCaptor.capture(), anyLong())).thenReturn(doc);

                // WHEN
                scraper.filter(filters);

                // THEN
                Map<String, String> capturedData = dataCaptor.getValue();
                assertThat(capturedData)
                        .containsEntry("RechIdSerie", "123")
                        .containsEntry("RechIdAuteur", "456")
                        .containsEntry("RechSerie", "Astérix")
                        .containsEntry("RechTitre", "Le bouclier Arverne")
                        .containsEntry("RechEditeur", "Dargaud")
                        .containsEntry("RechCollection", "La collection")
                        .containsEntry("RechStyle", "Humour")
                        .containsEntry("RechAuteur", "Goscinny")
                        .containsEntry("RechISBN", "978-2205001584")
                        .containsEntry("RechParution", "")
                        .containsEntry("RechOrigine", "")
                        .containsEntry("RechLangue", "")
                        .containsEntry("RechMotCle", "Gaulois")
                        .containsEntry("RechDLDeb", "01/01/1960")
                        .containsEntry("RechDLFin", "31/12/1970")
                        .containsEntry("RechCoteMin", "10")
                        .containsEntry("RechCoteMax", "100")
                        .containsEntry("RechEO", "on");
            }
        }
    }
}