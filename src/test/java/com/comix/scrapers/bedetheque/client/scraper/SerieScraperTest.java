package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.client.model.graphicnovel.GraphicNovel;
import com.comix.scrapers.bedetheque.client.model.serie.Serie;
import com.comix.scrapers.bedetheque.client.model.serie.SerieDetails;
import com.comix.scrapers.bedetheque.client.model.serie.SeriesByLetter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SerieScraperTest {

    @Mock
    private GraphicNovelScraper graphicNovelScraper;

    @InjectMocks
    private SerieScraper serieScraper;

    // Un fragment HTML complet pour une page de série
    private final String fullSerieHtml = """
        <html><body>
            <h1>Titre de la Série</h1>
            <ul class="serie-info">
                <li><label>Identifiant :</label> 12345</li>
                <li><label>Genre :</label> <span>Aventure</span></li>
                <li><label>Parution :</label> <span>Finie</span></li>
                <li><label>Origine :</label> France</li>
                <li><label>Langue :</label> Français</li>
                <li><label>Internet :</label> https://site-officiel.com</li>
                <li><label>Tomes :</label> 12</li>
            </ul>
            <div class="single-content serie"><p>Ceci est le synopsis.</p></div>
            <div class="serie-image">
                <a href="image_hd.jpg"><img src="image_thumb.jpg"></a>
            </div>
            <div class="copyrightserie">© Dargaud 2024</div>
            <div class="bandeau-menu"><ul><li><a href="avis.html">Avis <span>10</span></a></li></ul></div>
            <ul class="liste-albums"><li itemtype="https://schema.org/Book">...un album...</li></ul>
            <div class="alire"><div class="wrapper"><a href="serie-alire-1.html" title="Série à lire"><img src="cover_alire.jpg"></a></div></div>
        </body></html>
        """;

    @BeforeEach
    void setUp() {
        // Injection manuelle des dépendances @Value
        serieScraper.setBedethequeSerieIndexByLetterUrl("https://test.com/series-%s.html");
        serieScraper.setBedethequeSeriePrefixUrl("serie-");
        serieScraper.setLocalCacheActive(false);
        serieScraper.setLatency(0L);

        // Injection des chemins pour le téléchargement des médias
        ReflectionTestUtils.setField(serieScraper, "outputPageExampleThumbDirectory", "/tmp/thumbs/page/");
        ReflectionTestUtils.setField(serieScraper, "httpPageExampleThumbDirectory", "/media/thumbs/page/");
        ReflectionTestUtils.setField(serieScraper, "outputPageExampleHdDirectory", "/tmp/hd/page/");
        ReflectionTestUtils.setField(serieScraper, "httpPageExampleHdDirectory", "/media/hd/page/");
        ReflectionTestUtils.setField(serieScraper, "outputCoverFrontThumbDirectory", "/tmp/thumbs/cover/");
        ReflectionTestUtils.setField(serieScraper, "httpCoverFrontThumbDirectory", "/media/thumbs/cover/");
        ReflectionTestUtils.setField(serieScraper, "httpDefaultMediaFilename", "default.jpg");
    }

    @Nested
    @DisplayName("Tests for listAllSeriesIndexes()")
    class ListAllSeriesIndexesTests {
        @Test
        @DisplayName("should return all 27 alphabetical indexes")
        void shouldReturnAllAlphabeticalIndexes() {
            // WHEN
            List<SeriesByLetter> indexes = serieScraper.listAllSeriesIndexes();

            // THEN
            assertThat(indexes).hasSize(27);
            assertThat(indexes.getFirst().getLetter()).isEqualTo("0");
            assertThat(indexes.getFirst().getUrl()).isEqualTo("https://test.com/series-0.html");
            assertThat(indexes.get(26).getLetter()).isEqualTo("Z");
            assertThat(indexes.get(26).getUrl()).isEqualTo("https://test.com/series-Z.html");
        }
    }

    @Nested
    @DisplayName("Tests for listByLetter(letter)")
    class ListByLetterTests {
        @Test
        @DisplayName("should parse and return valid series from an index page")
        void shouldParseAndReturnValidSeries() {
            // GIVEN
            String html = """
                <div>
                    <a href="https://test.com/serie-1-Asterix.html">Astérix</a>
                    <a href="https://test.com/serie-2-Iznogoud.html">Iznogoud</a>
                    <a href="https://test.com/auteur-3-Goscinny.html">(AUT) Goscinny</a>
                    <a href="https://test.com/serie-4-Lucky-Luke.html">(AUT) Lucky Luke</a>
                </div>
                """;
            Document doc = Jsoup.parse(html);

            try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class)) {
                GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
                mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);
                when(mockScraper.load(anyString(), anyLong())).thenReturn(doc);

                // WHEN
                List<Serie> series = serieScraper.listByLetter("A");

                // THEN
                assertThat(series).hasSize(2);
                assertThat(series.get(0).getId()).isEqualTo("1");
                assertThat(series.get(0).getName()).isEqualTo("Astérix");
                assertThat(series.get(1).getId()).isEqualTo("2");
                assertThat(series.get(1).getName()).isEqualTo("Iznogoud");
            }
        }
    }

    @Nested
    @DisplayName("Tests for scrap(url)")
    class ScrapTests {

        @Test
        @DisplayName("should parse all fields correctly when HTML is complete")
        void shouldParseAllFieldsCorrectly() {
            // GIVEN
            Document doc = Jsoup.parse(fullSerieHtml);
            String url = "https://test.com/serie-12345-Titre-de-la-Série.html";

            // Mock des dépendances
            when(graphicNovelScraper.scrapFromSerie(anyString(), any(Document.class), any(Element.class)))
                    .thenReturn(new GraphicNovel()); // Retourne un album mocké

            try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class)) {
                GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
                mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);
                when(mockScraper.load(anyString(), anyLong())).thenReturn(doc);

                // WHEN
                SerieDetails result = serieScraper.scrap(url);

                // THEN
                assertThat(result).isNotNull();
                assertThat(result.getExternalId()).isEqualTo("12345");
                assertThat(result.getTitle()).isEqualTo("Titre de la Série");
                assertThat(result.getCategory()).isEqualTo("Aventure");
                assertThat(result.getStatus()).isEqualTo("Finie");
                assertThat(result.getOrigin()).isEqualTo("France");
                assertThat(result.getLanguage()).isEqualTo("Français");
                assertThat(result.getSiteUrl()).isEqualTo("https://site-officiel.com");
                assertThat(result.getSynopsys()).isEqualTo("Ceci est le synopsis.");
                assertThat(result.getPictureUrl()).isEqualTo("image_hd.jpg");
                assertThat(result.getPictureThbUrl()).isEqualTo("image_thumb.jpg");
                assertThat(result.getCopyright()).isEqualTo("© Dargaud 2024");
                assertThat(result.getScrapUrl()).isEqualTo(url);
                assertThat(result.getTomeCount()).isEqualTo(12);
                assertThat(result.getRatings().getCount()).isEqualTo(10);
                assertThat(result.getRatings().getUrl()).isEqualTo("avis.html");
                assertThat(result.getGraphicNovels()).hasSize(1);
                assertThat(result.getToReadSeries()).hasSize(1);
            }
        }

        @Test
        @DisplayName("should handle missing fields gracefully without errors")
        void shouldHandleMissingFieldsGracefully() {
            // GIVEN: Un HTML minimal
            String minimalHtml = "<html><body><h1>Titre Seul</h1></body></html>";
            Document doc = Jsoup.parse(minimalHtml);
            String url = "https://test.com/serie-minimal.html";

            try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class)) {
                GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
                mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);
                when(mockScraper.load(anyString(), anyLong())).thenReturn(doc);

                // WHEN
                SerieDetails result = serieScraper.scrap(url);

                // THEN
                assertThat(result).isNotNull();
                assertThat(result.getTitle()).isEqualTo("Titre Seul");
                // Les champs non trouvés doivent être null ou vides
                assertThat(result.getExternalId()).isNull();
                assertThat(result.getCategory()).isNull();
                assertThat(result.getSynopsys()).isNull();
                assertThat(result.getGraphicNovels()).isNotNull().isEmpty();
                assertThat(result.getToReadSeries()).isNotNull().isEmpty();
            }
        }

        @Test
        @DisplayName("should call downloadMedias when local cache is active")
        void shouldCallDownloadMediasWhenCacheIsActive() {
            // GIVEN
            SerieScraper scraperSpy = spy(serieScraper); // On espionne l'objet injecté
            scraperSpy.setLocalCacheActive(true);
            doReturn("local/path/image.jpg").when(scraperSpy).downloadMedia(anyString(), anyString(), anyString(), anyString(), anyString());

            Document doc = Jsoup.parse(fullSerieHtml);
            String url = "https://test.com/serie-12345.html";

            // Mock des dépendances externes
            when(graphicNovelScraper.scrapFromSerie(anyString(), any(Document.class), any(Element.class)))
                    .thenReturn(new GraphicNovel());

            try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class)) {
                GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
                mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);
                when(mockScraper.load(anyString(), anyLong())).thenReturn(doc);

                // WHEN
                scraperSpy.scrap(url);

                // THEN
                // On vérifie que downloadMedia a été appelé pour l'image de la série (thumb + hd) et la couverture "à lire"
                verify(scraperSpy, times(3)).downloadMedia(anyString(), anyString(), anyString(), anyString(), anyString());

                // Vérifications plus spécifiques
                verify(scraperSpy).downloadMedia(any(), any(), eq("image_thumb.jpg"), any(), any());
                verify(scraperSpy).downloadMedia(any(), any(), eq("image_hd.jpg"), any(), any());
                verify(scraperSpy).downloadMedia(any(), any(), eq("cover_alire.jpg"), any(), any());
            }
        }
    }
}