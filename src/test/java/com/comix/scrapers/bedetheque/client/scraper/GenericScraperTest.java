package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.exception.BusinessException;
import com.comix.scrapers.bedetheque.exception.TechnicalException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class GenericScraperTest {

    private static MockWebServer mockWebServer;
    private GenericScraper genericScraper;

    // @TempDir crée un répertoire temporaire pour chaque test et le nettoie après.
    @TempDir
    Path tempDir;

    @BeforeAll
    static void setUpAll() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDownAll() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void setUp() {
        genericScraper = new GenericScraper();
        ReflectionTestUtils.setField(genericScraper, "hashedDirectoryStep", 5000);
    }

    @Nested
    @DisplayName("Tests for downloadMedia(outputDir, httpPath, url)")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CoreDownloadMediaTests {

        @Test
        @Order(1)
        @DisplayName("doit retourner le chemin du fichier existant sans télécharger si le fichier est déjà présent")
        void shouldReturnExistingFilePath_whenFileAlreadyExists() throws IOException {
            // GIVEN: Un fichier existe déjà dans le répertoire temporaire
            String mediaFilename = "existing-image.jpg";
            Files.createFile(tempDir.resolve(mediaFilename));
            String httpMediaUrl = mockWebServer.url("/images/" + mediaFilename).toString();
            String outputHttpPath = "/media";

            // WHEN: On appelle la méthode de téléchargement
            String resultPath = genericScraper.downloadMedia(tempDir.toString() + "/", outputHttpPath, httpMediaUrl);

            // THEN: Le chemin retourné est correct et aucun appel n'a été fait au serveur
            assertThat(resultPath).isEqualTo(outputHttpPath + "/" + mediaFilename);
            assertThat(mockWebServer.getRequestCount()).isZero();
        }

        @Test
        @Order(2)
        @DisplayName("doit télécharger le fichier et le sauvegarder localement s'il n'existe pas")
        void shouldDownloadAndSaveFile_whenFileDoesNotExist() {
            // GIVEN: Le serveur est prêt à envoyer des données d'image
            String mediaFilename = "new-image.jpg";
            String imageData = "ceci-est-une-image";
            mockWebServer.enqueue(new MockResponse().setBody(imageData));
            String httpMediaUrl = mockWebServer.url("/images/" + mediaFilename).toString();
            String outputHttpPath = "/media";
            String tmpDir = tempDir.toString();

            // WHEN: On appelle la méthode de téléchargement
            String resultPath = genericScraper.downloadMedia(tmpDir, outputHttpPath, httpMediaUrl);

            // THEN: Le chemin retourné est correct
            assertThat(resultPath).isEqualTo(outputHttpPath + "/" + mediaFilename);

            // AND: Le fichier a été créé localement avec le bon contenu
            File downloadedFile = tempDir.resolve(mediaFilename).toFile();
            assertThat(downloadedFile).exists().hasContent(imageData);

            // AND: Une requête a bien été faite au serveur
            assertThat(mockWebServer.getRequestCount()).isEqualTo(1);
        }

        @Test
        @Order(3)
        @DisplayName("doit lancer une TechnicalException si le serveur retourne une erreur 404")
        void shouldThrowTechnicalException_whenServerReturns404() {
            // GIVEN: Le serveur retourne une erreur 404
            mockWebServer.enqueue(new MockResponse().setResponseCode(404));
            String httpMediaUrl = mockWebServer.url("/images/not-found.jpg").toString();

            // WHEN & THEN: On vérifie qu'une TechnicalException est lancée
            assertThatThrownBy(() -> genericScraper.downloadMedia(tempDir.toString(), "/media/", httpMediaUrl))
                    .isInstanceOf(Exception.class)
                    .isInstanceOf(TechnicalException.class)
                    .hasFieldOrPropertyWithValue("codeMessage", "ERR-SCR-003");
        }

        @Test
        @Order(4)
        @DisplayName("doit lancer une TechnicalException si l'URL est malformée")
        void shouldThrowTechnicalException_whenUrlIsInvalid() {
            // GIVEN: une URL invalide
            String invalidUrl = "h ttp://invalid-url.com/image.jpg";

            // WHEN & THEN: On vérifie qu'une TechnicalException est lancée
            assertThatThrownBy(() -> genericScraper.downloadMedia(tempDir.toString(), "/media/", invalidUrl))
                    .isInstanceOf(Exception.class)
                    .isInstanceOf(TechnicalException.class)
                    .hasFieldOrPropertyWithValue("codeMessage", "ERR-SCR-005");
        }
    }

    @Nested
    @DisplayName("Tests for downloadMedia(..., defaultFilename) - Safe Wrapper")
    class SafeDownloadMediaTests {

        @Test
        @DisplayName("doit retourner le chemin du fichier téléchargé si le téléchargement réussit")
        void shouldReturnDownloadedPath_whenDownloadSucceeds() {
            // GIVEN: On utilise un "espion" pour mocker la méthode interne tout en utilisant l'instance réelle.
            GenericScraper scraperSpy = Mockito.spy(genericScraper);

            String defaultPath = tempDir.resolve("media/default.jpg").toString();
            String mediaPath = tempDir.resolve("media/").toString();
            String expectedPath = mediaPath + "/0/downloaded.jpg";

            // On configure l'espion pour qu'il retourne un chemin de succès
            doReturn(expectedPath).when(scraperSpy).downloadMedia(anyString(), anyString(), anyString());

            // WHEN
            String result = scraperSpy.downloadMedia("/tmp", mediaPath, "https://a.com/b.jpg", defaultPath, "1");

            // THEN
            assertThat(result).isEqualTo(expectedPath);
        }

        @Test
        @DisplayName("doit retourner le chemin par défaut si le téléchargement échoue avec une BusinessException")
        void shouldReturnDefaultPath_whenDownloadFailsWithBusinessException() {
            // GIVEN
            GenericScraper scraperSpy = Mockito.spy(genericScraper);
            String defaultPath = tempDir.resolve("media/default.jpg").toString();
            String mediaPath = tempDir.resolve("media/").toString();

            // On configure l'espion pour qu'il lance une BusinessException
            doThrow(new BusinessException("ERR-TEST")).when(scraperSpy).downloadMedia(anyString(), anyString(), anyString());

            // WHEN
            String result = scraperSpy.downloadMedia("/tmp", mediaPath, "https://a.com/b.jpg", defaultPath, "1");

            // THEN
            assertThat(result).isEqualTo(defaultPath);
        }

        @Test
        @DisplayName("doit retourner le chemin par défaut si le téléchargement échoue avec une TechnicalException")
        void shouldReturnDefaultPath_whenDownloadFailsWithTechnicalException() {
            // GIVEN
            GenericScraper scraperSpy = Mockito.spy(genericScraper);
            String defaultPath = tempDir.resolve("media/default.jpg").toString();
            String mediaPath = tempDir.resolve("media/").toString();

            // On configure l'espion pour qu'il lance une TechnicalException
            doThrow(new TechnicalException("ERR-TEST", new IOException())).when(scraperSpy).downloadMedia(anyString(), anyString(), anyString());

            // WHEN
            String result = scraperSpy.downloadMedia("/tmp", mediaPath, "https://a.com/b.jpg", defaultPath, "1");

            // THEN
            assertThat(result).isEqualTo(defaultPath);
        }
    }
}
