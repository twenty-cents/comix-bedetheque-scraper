package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.client.model.rating.Rating;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingScraperTest {

    private RatingScraper scraper;

    // Un fragment HTML représentatif de la page des avis
    private final String ratingsHtml = """
        <html><body>
        <ol class="commentlist">
            <li>
                <div class="the-comment">
                    <div class="alignleft">
                        <a href="https://www.bedetheque.com/BD-Asterix-Tome-2-La-serpe-d-or-22942.html">
                            <img src="https://www.bedetheque.com/cache/thb_couv/asterix02eo_22942.jpg" title="Tome 2 - La serpe d'or">
                        </a>
                    </div>
                    <div class="comment-title">
                        <a href="https://www.bedetheque.com/BD-Asterix-Tome-2-La-serpe-d-or-22942.html">Tome 2. La serpe d'or</a>
                    </div>
                    <div class="comment-author">
                        <img src="https://www.bdgest.com/skin/stars2/5.png" title="Note : 5/5">
                        Avis de <a href="https://www.bedetheque.com/avis?u=crazybuyer1">crazybuyer1</a>
                        <small>Le 09/12/2020 à 11:51:30</small>
                    </div>
                    <div class="comment-text">
                        <p>Je me dois de prendre la plume...</p>
                    </div>
                </div>
            </li>
            <li>
                <div class="the-comment">
                    <!-- Un deuxième commentaire pour vérifier la taille de la liste -->
                </div>
            </li>
        </ol>
        </body></html>
        """;

    @BeforeEach
    void setUp() {
        scraper = new RatingScraper();
        // Injection manuelle des dépendances @Value
        scraper.setLatency(0L);
        scraper.setLocalCacheActive(false);

        // Utilisation de ReflectionTestUtils pour les champs privés sans setter
        ReflectionTestUtils.setField(scraper, "outputUserAvatarThumbDirectory", "/tmp/avatars/");
        ReflectionTestUtils.setField(scraper, "httpUserAvatarThumbDirectory", "/media/avatars/");
        ReflectionTestUtils.setField(scraper, "outputCoverFrontThumbDirectory", "/tmp/covers/");
        ReflectionTestUtils.setField(scraper, "httpCoverFrontThumbDirectory", "/media/covers/");
        ReflectionTestUtils.setField(scraper, "httpDefaultMediaFilename", "default.jpg");
    }

    @Nested
    @DisplayName("Tests de la logique de scraping")
    class ScrapingLogicTests {

        @Test
        @DisplayName("doit parser correctement tous les champs d'une page HTML valide")
        void shouldParseAllFieldsCorrectly() {
            // GIVEN
            Document doc = Jsoup.parse(ratingsHtml);
            String url = "https://test.com/ratings.html";

            try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class)) {
                GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
                mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);
                when(mockScraper.load(anyString(), anyLong())).thenReturn(doc);

                // WHEN
                List<Rating> ratings = scraper.scrap(url);

                // THEN
                assertThat(ratings).hasSize(2);

                Rating firstRating = ratings.getFirst();
                assertThat(firstRating.getGraphicNovelTitle()).isEqualTo("Tome 2. La serpe d'or");
                assertThat(firstRating.getGraphicNovelUrl()).isEqualTo("https://www.bedetheque.com/BD-Asterix-Tome-2-La-serpe-d-or-22942.html");
                assertThat(firstRating.getGraphicNovelPictureUrl()).isEqualTo("https://www.bedetheque.com/cache/thb_couv/asterix02eo_22942.jpg");
                assertThat(firstRating.getGraphicNovelPictureTitle()).isEqualTo("Tome 2 - La serpe d'or");
                assertThat(firstRating.getCreateBy()).isEqualTo("crazybuyer1");
                assertThat(firstRating.getCreateByAllRatingsUrl()).isEqualTo("https://www.bedetheque.com/avis?u=crazybuyer1");
                assertThat(firstRating.getCreateOn()).isEqualTo("Le 09/12/2020 à 11:51:30");
                assertThat(firstRating.getRatingPictureUrl()).isEqualTo("https://www.bdgest.com/skin/stars2/5.png");
                assertThat(firstRating.getRatingTitle()).isEqualTo("Note : 5/5");
                assertThat(firstRating.getComment()).isEqualTo("Je me dois de prendre la plume...");
            }
        }

        @Test
        @DisplayName("doit retourner une liste vide si aucun avis n'est trouvé")
        void shouldReturnEmptyListWhenNoRatingsFound() {
            // GIVEN
            Document doc = Jsoup.parse("<html><body><p>Aucun avis ici.</p></body></html>");
            String url = "https://test.com/no-ratings.html";

            try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class)) {
                GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
                mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);
                when(mockScraper.load(anyString(), anyLong())).thenReturn(doc);

                // WHEN
                List<Rating> ratings = scraper.scrap(url);

                // THEN
                assertThat(ratings).isNotNull().isEmpty();
            }
        }

        @Test
        @DisplayName("doit gérer les champs manquants sans lever d'exception")
        void shouldHandleMissingFieldsGracefully() {
            // GIVEN: HTML avec un bloc d'avis mais des éléments internes manquants
            String partialHtml = """
                <html><body>
                <ol class="commentlist">
                    <li>
                        <div class="the-comment">
                            <div class="comment-title">
                                <a href="https://url.com">Un Titre</a>
                            </div>
                            <!-- Auteur, texte du commentaire, etc. manquants -->
                        </div>
                    </li>
                </ol>
                </body></html>
                """;
            Document doc = Jsoup.parse(partialHtml);
            String url = "https://test.com/partial-ratings.html";

            try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class)) {
                GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
                mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);
                when(mockScraper.load(anyString(), anyLong())).thenReturn(doc);

                // WHEN
                List<Rating> ratings = scraper.scrap(url);

                // THEN
                assertThat(ratings).hasSize(1);
                Rating rating = ratings.getFirst();
                assertThat(rating.getGraphicNovelTitle()).isEqualTo("Un Titre");
                assertThat(rating.getGraphicNovelUrl()).isEqualTo("https://url.com");
                // Vérification que les champs manquants sont bien nuls
                assertThat(rating.getCreateBy()).isNull();
                assertThat(rating.getComment()).isNull();
                assertThat(rating.getRatingPictureUrl()).isNull();
            }
        }
    }

    @Nested
    @DisplayName("Tests du téléchargement des médias")
    class MediaDownloadingTests {

        @Test
        @DisplayName("doit appeler downloadMedia pour chaque image si le cache est actif")
        void shouldCallDownloadMediaWhenCacheIsActive() {
            // GIVEN
            // 1. Création d'un espion pour vérifier les appels de méthode sur l'objet réel
            RatingScraper scraperSpy = Mockito.spy(scraper);
            scraperSpy.setLocalCacheActive(true);

            // 2. Stub de la méthode downloadMedia pour éviter un vrai téléchargement
            doReturn("local/path/image.jpg")
                    .when(scraperSpy)
                    .downloadMedia(anyString(), anyString(), anyString(), anyString(), anyString());

            // 3. Préparation du document HTML
            Document doc = Jsoup.parse(ratingsHtml);
            String url = "https://test.com/ratings.html";

            try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class)) {
                GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
                mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);
                when(mockScraper.load(anyString(), anyLong())).thenReturn(doc);

                // WHEN
                scraperSpy.scrap(url);

                // THEN
                // Vérification que downloadMedia a été appelé pour la couverture de l'album et l'image de notation
                // Notre HTML de test contient un bloc d'avis complet, donc 2 images à télécharger.
                verify(scraperSpy, times(2)).downloadMedia(anyString(), anyString(), anyString(), anyString(), anyString());

                // Vérifications plus spécifiques
                verify(scraperSpy).downloadMedia(
                        "/tmp/covers/",
                        "/media/covers/",
                        "https://www.bedetheque.com/cache/thb_couv/asterix02eo_22942.jpg",
                        "default.jpg",
                        "22942"
                );
                verify(scraperSpy).downloadMedia(
                        "/tmp/avatars/",
                        "/media/avatars/",
                        "https://www.bdgest.com/skin/stars2/5.png",
                        "default.jpg",
                        "1"
                );
            }
        }

        @Test
        @DisplayName("ne doit PAS appeler downloadMedia si le cache est inactif")
        void shouldNotCallDownloadMediaWhenCacheIsInactive() {
            // GIVEN
            RatingScraper scraperSpy = Mockito.spy(scraper);
            scraperSpy.setLocalCacheActive(false); // Par défaut, mais explicite pour la clarté

            Document doc = Jsoup.parse(ratingsHtml);
            String url = "https://test.com/ratings.html";

            try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class)) {
                GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
                mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);
                when(mockScraper.load(anyString(), anyLong())).thenReturn(doc);

                // WHEN
                scraperSpy.scrap(url);

                // THEN
                // Vérification que downloadMedia n'a jamais été appelé
                verify(scraperSpy, never()).downloadMedia(anyString(), anyString(), anyString(), anyString(), anyString());
            }
        }
    }
}