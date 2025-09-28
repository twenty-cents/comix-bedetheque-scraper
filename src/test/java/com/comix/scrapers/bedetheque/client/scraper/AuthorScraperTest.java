package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.client.model.author.Author;
import com.comix.scrapers.bedetheque.client.model.author.AuthorDetails;
import com.comix.scrapers.bedetheque.client.model.author.AuthorsByLetter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorScraperTest {

    private AuthorScraper authorScraper;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        authorScraper = new AuthorScraper();
        // On injecte manuellement les valeurs de configuration pour le test
        authorScraper.setBedethequeAuthorsListByLetter("http://test.com/authors/0.html");
        authorScraper.setBedethequeAuthorPrefixUrl("__AUTEUR-");
        authorScraper.setLocalCacheActive(false); // Désactivé par défaut pour les tests unitaires
        authorScraper.setLatency(0L);
    }

    @Nested
    @DisplayName("Tests for getBedethequeId(url)")
    class GetBedethequeIdTests {

        @Test
        @DisplayName("doit extraire l'ID d'une URL valide")
        void shouldExtractIdFromValidUrl() {
            String url = "https://www.bedetheque.com/auteur-12345-Some-Name.html";
            assertThat(authorScraper.getBedethequeId(url)).isEqualTo("12345");
        }

        @Test
        @DisplayName("doit retourner une chaîne vide pour une URL invalide")
        void shouldReturnEmptyStringForInvalidUrl() {
            String url = "https://www.bedetheque.com/auteur.html";
            assertThat(authorScraper.getBedethequeId(url)).isEmpty();
        }

        @Test
        @DisplayName("doit retourner une chaîne vide pour une URL vide ou nulle")
        void shouldReturnEmptyStringForBlankUrl() {
            assertThat(authorScraper.getBedethequeId("")).isEmpty();
            assertThat(authorScraper.getBedethequeId(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tests for author indexes")
    class AuthorIndexesTests {

        @Test
        @DisplayName("getIndexUrlsByLetter doit construire l'URL correctement")
        void getIndexUrlsByLetter_shouldBuildUrlCorrectly() {
            assertThat(authorScraper.getIndexUrlsByLetter("A")).isEqualTo("http://test.com/authors/A.html");
            assertThat(authorScraper.getIndexUrlsByLetter("Z")).isEqualTo("http://test.com/authors/Z.html");
        }

        @Test
        @DisplayName("listAllAuthorsIndexes doit retourner tous les index alphabétiques")
        void listAllAuthorsIndexes_shouldReturnAllAlphabeticalIndexes() {
            List<AuthorsByLetter> indexes = authorScraper.listAllAuthorsIndexes();
            assertThat(indexes).hasSize(27); // 0-9 + A-Z
            assertThat(indexes.get(1).getLetter()).isEqualTo("A");
            assertThat(indexes.get(1).getUrl()).isEqualTo("http://test.com/authors/A.html");
        }
    }

    @Nested
    @DisplayName("Tests for scrapAuthorsIndexedByLetter(letter)")
    class ScrapAuthorsIndexedByLetterTests {

        @Test
        @DisplayName("doit retourner une liste d'auteurs quand la page contient des liens")
        void shouldReturnAuthorListWhenPageHasLinks() {
            // GIVEN: Un document HTML simulé avec des liens d'auteurs
            String html = "<div>" +
                    "<a href='http://other.com'>Invalid</a>" +
                    "<a href='http://test.com/__AUTEUR-1-Goscinny.html'>Goscinny, René</a>" +
                    "<a href='http://test.com/__AUTEUR-2-Uderzo.html'>Uderzo, Albert</a>" +
                    "</div>";
            Document doc = Jsoup.parse(html);

            // On mock le singleton pour qu'il ne fasse pas d'appel réseau
            try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class)) {
                GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
                mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);
                when(mockScraper.load(anyString(), anyLong())).thenReturn(doc);

                // WHEN: On appelle la méthode à tester
                List<Author> authors = authorScraper.scrapAuthorsIndexedByLetter("G");

                // THEN: On vérifie que les auteurs ont été correctement extraits
                assertThat(authors).hasSize(2);
                assertThat(authors.getFirst().getId()).isEqualTo("1");
                assertThat(authors.getFirst().getName()).isEqualTo("Goscinny, René");
                assertThat(authors.getFirst().getUrl()).isEqualTo("http://test.com/__AUTEUR-1-Goscinny.html");
            }
        }
    }

    @Nested
    @DisplayName("Tests for scrap(author)")
    class ScrapAuthorDetailsTests {

        @Test
        @DisplayName("doit extraire tous les détails d'un auteur à partir d'un document complet")
        void shouldScrapAllDetailsFromCompleteDocument() {
            // GIVEN: Un document HTML simulé complet
            String html = "<html><body>" +
                    "<ul class='auteur-info'>" +
                    "<li><label>Identifiant :</label>123</li>" +
                    "<li><label>Nom :</label><span>DOE</span></li>" +
                    "<li><label>Prénom :</label><span>John</span></li>" +
                    "<li><label>Pseudo :</label>Johnny</li>" +
                    "<li><label>Naissance :</label>le 01/01/1970 <span class='pays-auteur'>(France)</span></li>" +
                    "<li><label>Décès :</label>le 31/12/2020</li>" +
                    "<li><label>Site :</label><a href='http://john.doe'>john.doe</a></li>" +
                    "<li><label>Voir aussi :</label><a href='http://test.com/__AUTEUR-456-Alias.html'>Alias, John</a></li>" +
                    "</ul>" +
                    "<div class='auteur-image'><a href='photo.jpg'><img src='photo_thb.jpg'></a></div>" +
                    "<p class='bio'>Une biographie intéressante.</p>" +
                    "</body></html>";
            Document doc = Jsoup.parse(html);
            Author authorToScrap = new Author("123", "DOE, John", "http://test.com/__AUTEUR-123-Doe.html");

            try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class)) {
                GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
                mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);
                when(mockScraper.load(anyString(), anyLong())).thenReturn(doc);

                // WHEN
                AuthorDetails details = authorScraper.scrap(authorToScrap);

                // THEN
                assertThat(details.getId()).isEqualTo("123");
                assertThat(details.getLastname()).isEqualTo("DOE");
                assertThat(details.getFirstname()).isEqualTo("John");
                assertThat(details.getNickname()).isEqualTo("Johnny");
                assertThat(details.getBirthdate()).isEqualTo("01/01/1970");
                assertThat(details.getDeceaseDate()).isEqualTo("31/12/2020");
                assertThat(details.getNationality()).isEqualTo("France");
                assertThat(details.getSiteUrl()).isEqualTo("http://john.doe");
                assertThat(details.getBiography()).isEqualTo("Une biographie intéressante.");
                assertThat(details.getPhotoUrl()).isEqualTo("photo.jpg");
                assertThat(details.getPhotoThbUrl()).isEqualTo("photo_thb.jpg");
                assertThat(details.getOtherAuthorPseudonym()).isNotNull();
                assertThat(details.getOtherAuthorPseudonym().getId()).isEqualTo("456");
                assertThat(details.getOtherAuthorPseudonym().getName()).isEqualTo("Alias, John");
            }
        }

        @Test
        @DisplayName("doit gérer les champs optionnels manquants sans erreur")
        void shouldHandleMissingOptionalFields() {
            // GIVEN: Un document HTML avec des champs manquants (pas de décès, pas de site, etc.)
            String html = "<html><body>" +
                    "<ul class='auteur-info'>" +
                    "<li><label>Identifiant :</label>789</li>" +
                    "<li>Nom : <span>SMITH</span></li>" +
                    "</ul>" +
                    "<p class='bio'></p>" + // Bio vide
                    "</body></html>";
            Document doc = Jsoup.parse(html);
            Author authorToScrap = new Author("789", "SMITH", "http://test.com/__AUTEUR-789-Smith.html");

            try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class)) {
                GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
                mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);
                when(mockScraper.load(anyString(), anyLong())).thenReturn(doc);

                // WHEN
                AuthorDetails details = authorScraper.scrap(authorToScrap);

                // THEN
                assertThat(details.getId()).isEqualTo("789");
                assertThat(details.getLastname()).isEqualTo("SMITH");
                assertThat(details.getFirstname()).isNull();
                assertThat(details.getDeceaseDate()).isNull();
                assertThat(details.getSiteUrl()).isNull();
                assertThat(details.getBiography()).isEmpty();
                assertThat(details.getOtherAuthorPseudonym()).isNull();
            }
        }
    }

    @Test
    @DisplayName("doit appeler downloadMedias pour chaque image quand le cache local est actif")
    void scrap_shouldCallDownloadMedias_whenCacheIsActive() {
        // GIVEN:
        // 1. On crée un "espion" de notre scraper pour pouvoir vérifier les appels à ses propres méthodes.
        AuthorScraper scraperSpy = Mockito.spy(new AuthorScraper());

        // 2. On configure l'espion avec les valeurs nécessaires.
        scraperSpy.setLocalCacheActive(true); // IMPORTANT: On active le cache
        scraperSpy.setLatency(0L);
        // On injecte les valeurs des champs privés via la réflexion.
        // On construit un chemin sûr à l'intérieur du répertoire temporaire
        String outputAuthorThumbDirectory = tempDir.resolve("path/author/thumbs").toString();
        String outputAuthorHdDirectory = tempDir.resolve("path/author/hd").toString();
        String outputCoverFrontThumbDirectory = tempDir.resolve("path/cover/thumbs").toString();

        ReflectionTestUtils.setField(scraperSpy, "outputAuthorThumbDirectory", outputAuthorThumbDirectory);
        ReflectionTestUtils.setField(scraperSpy, "httpAuthorThumbPath", "http://media/author/thumbs");
        ReflectionTestUtils.setField(scraperSpy, "outputAuthorHdDirectory", outputAuthorHdDirectory);
        ReflectionTestUtils.setField(scraperSpy, "httpAuthorHdPath", "http://media/author/hd");
        ReflectionTestUtils.setField(scraperSpy, "outputCoverFrontThumbDirectory", outputCoverFrontThumbDirectory);
        ReflectionTestUtils.setField(scraperSpy, "httpCoverFrontThumbDirectory", "http://media/cover/thumbs");
        ReflectionTestUtils.setField(scraperSpy, "httpDefaultMediaFilename", "default.jpg");

        // 3. On prépare un document HTML simple avec les images à télécharger.
        String html = "<html><body>" +
                "<ul class='auteur-info'><li>Identifiant : 123</li></ul>" +
                "<div class='auteur-image'><a href='http://external.com/photo.jpg'><img src='http://external.com/photo_thb.jpg'></a></div>" +
                "<div class='tab_content'><ul class='gallery-side'><a href='http://serie.com/serie-1'><img src='http://external.com/cover.jpg'><span>Serie 1</span></a></ul></div>" +
                "</body></html>";
        Document doc = Jsoup.parse(html);
        Author authorToScrap = new Author("123", "DOE, John", "http://test.com/__AUTEUR-123-Doe.html");

        // 4. On "stub" la méthode downloadMedia pour qu'elle ne fasse pas de vrai téléchargement.
        // On utilise doReturn().when(spy) pour les espions.
        doReturn("local_path").when(scraperSpy).downloadMedia(anyString(), anyString(), anyString(), anyString(), anyString());

        // 5. On mock le singleton pour qu'il retourne notre document contrôlé.
        try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class)) {
            GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
            mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);
            when(mockScraper.load(anyString(), anyLong())).thenReturn(doc);

            // WHEN:
            scraperSpy.scrap(authorToScrap);

            // THEN:
            // On vérifie que downloadMedia a été appelé pour chaque image avec les bons paramètres.
            // Photo de l'auteur (HD)
            verify(scraperSpy, times(1)).downloadMedia(
                    outputAuthorHdDirectory,
                    "http://media/author/hd",
                    "http://external.com/photo.jpg",
                    "default.jpg",
                    "Identifiant : 123"
            );
            // Photo de l'auteur (miniature)
            verify(scraperSpy, times(1)).downloadMedia(
                    outputAuthorThumbDirectory,
                    "http://media/author/thumbs",
                    "http://external.com/photo_thb.jpg",
                    "default.jpg",
                    "Identifiant : 123"
            );
            // Couverture de la série à découvrir
            verify(scraperSpy, times(1)).downloadMedia(
                    outputCoverFrontThumbDirectory,
                    "http://media/cover/thumbs",
                    "http://external.com/cover.jpg",
                    "default.jpg",
                    null
            );
        }
    }
}