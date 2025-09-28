package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.client.model.graphicnovel.GraphicNovel;
import com.comix.scrapers.bedetheque.client.model.graphicnovel.GraphicNovelPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphicNovelScraperTest {

    private GraphicNovelScraper scraper;

    // Un fragment HTML complet pour un album, utilisé dans plusieurs tests
    private final String albumHtml = """
        <li itemtype="https://schema.org/Book">
            <h3 class="titre">
                <a class="titre">
                    <span itemprop="name">1<span class="numa">(a)</span>. Le titre de l'album</span>
                </a>
            </h3>
            <ul class="infos">
                <li><label>Identifiant :</label> 12345</li>
                <li><label>Scénario :</label><a href="auteur-1-BD.html"><span>Goscinny, René</span></a></li>
                <li><label>Dessin :</label><a href="auteur-2-BD.html"><span>Uderzo, Albert</span></a></li>
                <li><label>Dépot légal :</label> 01/2024 <span>(Parution le 02/2024)</span></li>
                <li><label>Editeur :</label> <span>Dargaud</span></li>
                <li><label>Collection :</label> <a href="collection-1.html">La Collection</a></li>
                <li><label>ISBN :</label> <span>978-2205001584</span></li>
                <li><label>Planches :</label> <span>48</span></li>
                <li><label>Format :</label> Normal</li>
                <li><label>Autres infos :</label> <i class="icon-star"></i><i class="icon-pause"></i><i class="icon-tag"></i></li>
            </ul>
            <div class="autres">
                <p>Info édition</p>
                <a href="reedition-1.html"><em>Rééditions</em> (<strong>3</strong>)</a>
            </div>
            <div class="album-side">
                <div class="couv"><img src="couv_thumb.jpg"><span>© Dargaud</span></div>
                <div class="sous-couv">
                    <a class="browse-couvertures" href="couv_hd.jpg"></a>
                    <a class="browse-versos" href="verso_hd.jpg"><img src="verso_thumb.jpg"></a>
                    <a class="browse-planches" href="planche_hd.jpg"><img src="planche_thumb.jpg"></a>
                </div>
            </div>
            <div class="eval">
                <ul class="unit-rating"><li style="width: 80%;"></li></ul>
                <p class="message">Note: <strong>4/5</strong> (10 votes)</p>
            </div>
        </li>
        """;

    @BeforeEach
    void setUp() {
        scraper = new GraphicNovelScraper();
        // Injection manuelle des dépendances @Value
        scraper.setLatency(0L);
        scraper.setLocalCacheActive(false);

        // On utilise ReflectionTestUtils car les champs n'ont pas de setters publics.
        ReflectionTestUtils.setField(scraper, "outputCoverFrontThumbDirectory", "/tmp/thumbs/front/");
        ReflectionTestUtils.setField(scraper, "httpCoverFrontThumbDirectory", "/media/thumbs/front/");
        ReflectionTestUtils.setField(scraper, "outputCoverBackThumbDirectory", "/tmp/thumbs/back/");
        ReflectionTestUtils.setField(scraper, "httpCoverBackThumbDirectory", "/media/thumbs/back/");
        ReflectionTestUtils.setField(scraper, "outputPageExampleThumbDirectory", "/tmp/thumbs/page/");
        ReflectionTestUtils.setField(scraper, "httpPageExampleThumbDirectory", "/media/thumbs/page/");
        ReflectionTestUtils.setField(scraper, "outputCoverFrontHdDirectory", "/tmp/hd/front/");
        ReflectionTestUtils.setField(scraper, "httpCoverFrontHdDirectory", "/media/hd/front/");
        ReflectionTestUtils.setField(scraper, "outputCoverBackHdDirectory", "/tmp/hd/back/");
        ReflectionTestUtils.setField(scraper, "httpCoverBackHdDirectory", "/media/hd/back/");
        ReflectionTestUtils.setField(scraper, "outputPageExampleHdDirectory", "/tmp/hd/page/");
        ReflectionTestUtils.setField(scraper, "httpPageExampleHdDirectory", "/media/hd/page/");
        ReflectionTestUtils.setField(scraper, "httpDefaultMediaFilename", "default.jpg");
    }

    @Nested
    @DisplayName("Tests for scrapIdFromUrl(url)")
    class ScrapIdFromUrlTests {
        @Test
        @DisplayName("should extract ID from a valid URL")
        void shouldExtractIdFromValidUrl() {
            String url = "https://www.bedetheque.com/BD-Asterix-Tome-2-La-serpe-d-or-22942.html";
            assertThat(GraphicNovelScraper.scrapIdFromUrl(url)).isEqualTo("22942");
        }

        @Test
        @DisplayName("should return null for a URL without a numeric ID at the end")
        void shouldReturnNullForUrlWithoutNumericId() {
            String url = "https://www.bedetheque.com/BD-Asterix.html";
            assertThat(GraphicNovelScraper.scrapIdFromUrl(url)).isNull();
        }

        @Test
        @DisplayName("should return null for a blank URL")
        void shouldReturnNullForBlankUrl() {
            assertThat(GraphicNovelScraper.scrapIdFromUrl("")).isNull();
            assertThat(GraphicNovelScraper.scrapIdFromUrl(null)).isNull();
        }
    }

    @Nested
    @DisplayName("Tests for scrapElement(url, page)")
    class ScrapPaginatedTests {
        @Test
        @DisplayName("should build the correct URL and parse pagination info")
        void shouldBuildUrlAndParsePagination() {
            // GIVEN
            String baseUrl = "https://test.com/serie-1.html";
            String pagedUrl = "https://test.com/serie-1__2.html"; // page 3 -> __2.html

            String html = """
                <div>
                    <div class="bandeau-menu"><ul><li><a>Albums <span>25</span></a></li></ul></div>
                    <ul class="liste-albums">
                        %s
                    </ul>
                </div>
                """.formatted(albumHtml);
            Document doc = Jsoup.parse(html);

            try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class)) {
                GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
                mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);
                // Mock both load calls
                when(mockScraper.load(eq(pagedUrl), anyLong())).thenReturn(doc);

                // WHEN
                // We need to spy the scraper to mock the internal call to scrapFromSerie
                GraphicNovelScraper scraperSpy = spy(scraper);
                doReturn(List.of(new GraphicNovel())).when(scraperSpy).scrapFromSerie(anyString());

                GraphicNovelPage result = scraperSpy.scrapElement(baseUrl, 3);

                // THEN
                // 1. Verify the correct URL was loaded
                verify(mockScraper).load(eq(pagedUrl), anyLong());

                // 2. Verify pagination calculation
                assertThat(result.getPage()).isEqualTo(3);
                assertThat(result.getTotalElements()).isEqualTo(25);
                assertThat(result.getTotalPages()).isEqualTo(3); // 25 / 10 -> 2.5 -> 3
                assertThat(result.getSize()).isEqualTo(10);
                assertThat(result.getGraphicNovels()).hasSize(1);
            }
        }
    }

    @Nested
    @DisplayName("Core Scraping Logic Tests")
    class CoreScrapingLogicTests {

        @Test
        @DisplayName("should parse all fields correctly when HTML is complete")
        void shouldParseAllFieldsCorrectly() {
            // GIVEN
            // On enveloppe le fragment HTML dans la structure attendue par la méthode publique
            String fullPageHtml = String.format("<ul class=\"liste-albums\">%s</ul>", albumHtml);
            Document doc = Jsoup.parse(fullPageHtml);

            // WHEN
            // On appelle la méthode publique qui, à son tour, appellera la méthode privée
            List<GraphicNovel> results = scraper.scrapElement("https://test.com/serie.html", doc);

            // THEN
            // On vérifie que la liste contient bien notre résultat
            assertThat(results).isNotNull().hasSize(1);
            GraphicNovel result = results.getFirst();

            // Les assertions sur l'objet
            checkGraphicNovel(result);

            assertThat(result.getRatings()).isNotNull();
            assertThat(result.getRatings().getRating()).isEqualTo("4/5");

            assertThat(result.getAuthors()).hasSize(2);
            assertThat(result.getAuthors().getFirst().getName()).isEqualTo("Goscinny, René");
            assertThat(result.getAuthors().getFirst().getRole()).isEqualTo("Scénario");
        }

        private void checkGraphicNovel(GraphicNovel result) {
            assertThat(result.getTome()).isEqualTo("1");
            assertThat(result.getTomeNum()).isEqualTo(1);
            assertThat(result.getNumEdition()).isEqualTo("(a)");
            assertThat(result.getTitle()).isEqualTo("Le titre de l'album");
            assertThat(result.getExternalId()).isEqualTo("12345");
            assertThat(result.getPublicationDate()).isEqualTo("01/2024");
            assertThat(result.getReleaseDate()).isEqualTo("02/2024");
            assertThat(result.getPublisher()).isEqualTo("Dargaud");
            assertThat(result.getCollection()).isEqualTo("La Collection");
            assertThat(result.getCollectionUrl()).isEqualTo("collection-1.html");
            assertThat(result.getIsbn()).isEqualTo("978-2205001584");
            assertThat(result.getTotalPages()).isEqualTo(48);
            assertThat(result.getFormat()).isEqualTo("Normal");
            assertThat(result.getIsOriginalPublication()).isTrue();
            assertThat(result.getIsIntegrale()).isTrue();
            assertThat(result.getIsBroche()).isTrue();
            assertThat(result.getInfoEdition()).isEqualTo("Info édition");
            assertThat(result.getReeditionUrl()).isEqualTo("reedition-1.html");
            assertThat(result.getReeditionCount()).isEqualTo("3");
            assertThat(result.getCoverPictureUrl()).isEqualTo("couv_hd.jpg");
            assertThat(result.getCoverThumbnailUrl()).isEqualTo("couv_thumb.jpg");
            assertThat(result.getBackCoverPictureUrl()).isEqualTo("verso_hd.jpg");
            assertThat(result.getBackCoverThumbnailUrl()).isEqualTo("verso_thumb.jpg");
            assertThat(result.getPagePictureUrl()).isEqualTo("planche_hd.jpg");
            assertThat(result.getPageThumbnailUrl()).isEqualTo("planche_thumb.jpg");
            assertThat(result.getCopyright()).isEqualTo("© Dargaud");
            assertThat(result.getScrapUrl()).isEqualTo("https://test.com/serie.html");
        }

        @Test
        @DisplayName("should handle missing fields gracefully")
        void shouldHandleMissingFields() {
            // GIVEN
            String minimalHtml = "<li itemtype='https://schema.org/Book'><h3 class='titre'><a class='titre'><span itemprop='name'> 1<span class='numa'></span>. Titre</span></a></h3></li>";

            String fullPageHtml = String.format("<ul class=\"liste-albums\">%s</ul>", minimalHtml);
            Document doc = Jsoup.parse(fullPageHtml);

            // WHEN
            List<GraphicNovel> results = scraper.scrapElement("https://test.com/serie.html", doc);

            // THEN
            assertThat(results).isNotNull().hasSize(1);
            GraphicNovel result = results.getFirst();

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Titre");
            // Assert that fields that were not in the HTML are null or default
            assertThat(result.getIsbn()).isNull();
            assertThat(result.getPublisher()).isNull();
            assertThat(result.getTotalPages()).isNull();
            assertThat(result.getIsOriginalPublication()).isFalse();
            assertThat(result.getAuthors()).isEmpty();
        }

        @Test
        @DisplayName("should call downloadMedias when local cache is active")
        void shouldCallDownloadMediasWhenCacheIsActive() {
            // GIVEN
            GraphicNovelScraper scraperSpy = Mockito.spy(scraper);
            scraperSpy.setLocalCacheActive(true);
            doReturn("local/path")
                    .when(scraperSpy)
                    .downloadMedia(any(), any(), any(), any(), any());

            String fullPageHtml = String.format("<ul class=\"liste-albums\">%s</ul>", albumHtml);
            Document doc = Jsoup.parse(fullPageHtml);

            // WHEN
            // On appelle la méthode publique sur l'espion
            scraperSpy.scrapElement("https://test.com/serie.html", doc);

            // THEN
            // Verify that downloadMedia was called for all 6 images (3 thumbs, 3 HD)
            verify(scraperSpy, times(6)).downloadMedia(any(), any(), any(), any(), any());
        }
    }

}