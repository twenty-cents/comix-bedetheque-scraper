package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.client.model.statistics.GlobalStatistics;
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

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalStatisticsScraperTest {

    private GlobalStatisticsScraper scraper;

    // Un fragment HTML complet pour le cas nominal
    private final String fullHtml = """
        <html>
            <body>
                <ul class="stats">
                    <li>Séries <span>123 456</span></li>
                    <li>Albums <span>789 012</span></li>
                    <li>Auteurs <span>34 567</span></li>
                    <li>Revues <span>8 901</span></li>
                </ul>
                <div class="magazine-widget">
                    <a href="BD-Titre-Principal-1.html"><img src="couv_princ.jpg"></a>
                    <div class="big-desc">
                        <h3><a href="#">Série Principale</a></h3>
                        <h4>1. Titre Principal</h4>
                        <div class="magz-meta">Éditeur Principal| 01/2024</div>
                        <p>Synopsis principal...</p>
                    </div>
                </div>
                <ul class="gallery-couv-large">
                    <li>
                        <a href="BD-Titre-Nouveaute-2.html" title="Tome 2. Titre Nouveauté"></a>
                        <img src="couv_nouv.jpg">
                        <span class="titre">Tome 2. Titre Nouveauté</span>
                        <span class="editeur">Éditeur Nouveauté</span>
                    </li>
                </ul>
                <ul class="gallery-couv">
                    <li>
                        <a href="BD-Dernier-Ajout-3.html">
                            <img src="couv_ajout.jpg">
                        </a>
                        <span class="titre"><b>Série Dernier Ajout</b> - Tome 3</span>
                    </li>
                </ul>
            </body>
        </html>
        """;

    @BeforeEach
    void setUp() {
        scraper = new GlobalStatisticsScraper();
        // Injection manuelle des dépendances @Value
        scraper.setBedethequeUrl("https://test.com");
        scraper.setLatency(0L);
        scraper.setLocalCacheActive(false);
        // Les valeurs pour le téléchargement ne sont pas nécessaires pour la plupart des tests,
        // mais sont requises pour le test avec cache actif.
    }

    @Nested
    @DisplayName("Tests for scrap() method")
    class ScrapTests {

        @Test
        @DisplayName("should parse all sections correctly when HTML is complete and cache is inactive")
        void shouldParseAllSectionsWhenHtmlIsComplete() {
            // GIVEN: Un document HTML complet
            Document doc = Jsoup.parse(fullHtml);

            // On mock les appels statiques pour isoler le test
            try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class);
                 MockedStatic<GraphicNovelScraper> mockedGnScraper = Mockito.mockStatic(GraphicNovelScraper.class)) {

                // Préparation du mock pour le singleton de scraping
                GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
                mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);
                when(mockScraper.load(anyString(), anyLong())).thenReturn(doc);

                // Préparation du mock pour l'extraction d'ID
                mockedGnScraper.when(() -> GraphicNovelScraper.scrapIdFromUrl(anyString())).thenAnswer(invocation -> {
                    String url = invocation.getArgument(0);
                    if (url.contains("1.html")) return "1";
                    if (url.contains("2.html")) return "2";
                    if (url.contains("3.html")) return "3";
                    return "unknown";
                });

                // WHEN: On appelle la méthode à tester
                GlobalStatistics stats = scraper.scrap();

                // THEN: On vérifie que toutes les données ont été extraites correctement
                assertThat(stats.getSeries()).isEqualTo(123456);
                assertThat(stats.getGraphicNovels()).isEqualTo(789012);
                assertThat(stats.getAuthors()).isEqualTo(34567);
                assertThat(stats.getReviews()).isEqualTo(8901);

                // Vérification des nouveautés (1 principale + 1 de la liste)
                assertThat(stats.getNews()).hasSize(2);
                assertThat(stats.getNews().getFirst().getId()).isEqualTo("1");
                assertThat(stats.getNews().getFirst().getSerieTitle()).isEqualTo("Série Principale");
                assertThat(stats.getNews().getFirst().getTitle()).isEqualTo("Titre Principal");
                assertThat(stats.getNews().getFirst().getTome()).isEqualTo("1");
                assertThat(stats.getNews().getFirst().getPublisher()).isEqualTo("Éditeur Principal");

                // Vérification des derniers ajouts
                assertThat(stats.getLastEntries()).hasSize(1);
                assertThat(stats.getLastEntries().getFirst().getId()).isEqualTo("3");
                assertThat(stats.getLastEntries().getFirst().getSerieTitle()).isEqualTo("Série Dernier Ajout");
                assertThat(stats.getLastEntries().getFirst().getTome()).isEqualTo("3");
            }
        }

        @Test
        @DisplayName("should handle missing sections gracefully")
        void shouldHandleMissingSections() {
            // GIVEN: Un document HTML où il manque la section "derniers ajouts"
            String partialHtml = """
                <html><body>
                    <ul class="stats"><li>Séries <span>123</span></li></ul>
                </body></html>
                """;
            Document doc = Jsoup.parse(partialHtml);

            try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class)) {
                GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
                mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);
                when(mockScraper.load(anyString(), anyLong())).thenReturn(doc);

                // WHEN
                GlobalStatistics stats = scraper.scrap();

                // THEN
                assertThat(stats.getSeries()).isEqualTo(123);
                assertThat(stats.getGraphicNovels()).isNull(); // La méthode scrapElement retourne null si non trouvé
                assertThat(stats.getNews()).isNotNull().isEmpty();
                assertThat(stats.getLastEntries()).isNotNull().isEmpty();
            }
        }

        @Test
        @DisplayName("should call downloadMedia for each image when cache is active")
        void shouldCallDownloadMediaWhenCacheIsActive() {
            // GIVEN: On utilise un espion (spy) pour vérifier les appels à ses propres méthodes
            GlobalStatisticsScraper scraperSpy = Mockito.spy(scraper);
            scraperSpy.setLocalCacheActive(true); // Activation du cache
            Document doc = Jsoup.parse(fullHtml);

            // On stub la méthode de téléchargement pour qu'elle ne fasse rien mais puisse être vérifiée
            doReturn("local/path/image.jpg").when(scraperSpy).downloadMedia(any(), any(), any(), any(), any());

            try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class);
                 MockedStatic<GraphicNovelScraper> mockedGnScraper = Mockito.mockStatic(GraphicNovelScraper.class)) {

                GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
                mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);
                when(mockScraper.load(anyString(), anyLong())).thenReturn(doc);
                mockedGnScraper.when(() -> GraphicNovelScraper.scrapIdFromUrl(anyString())).thenReturn("id");

                // WHEN
                scraperSpy.scrap();

                // THEN: On vérifie que le téléchargement a été tenté pour chaque image trouvée
                // 1 (principale) + 1 (nouveauté) + 1 (dernier ajout) = 3 images
                verify(scraperSpy, times(3)).downloadMedia(any(), any(), anyString(), any(), any());
            }
        }
    }
}