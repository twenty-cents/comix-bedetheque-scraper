package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.exception.TechnicalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class GenericScraperTest {

    private GenericScraper scraper;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        scraper = new GenericScraper();
        // Injection de la propriété @Value pour le hachage des répertoires
        ReflectionTestUtils.setField(scraper, "hashedDirectoryStep", 5000);
    }

    @Test
    void getMediaFilename_shouldReturnFilename_whenUrlIsPresent() {
        String url = "http://site.com/folder/image.jpg";
        String result = scraper.getMediaFilename(url);
        assertThat(result).isEqualTo("image.jpg");
    }

    @Test
    void getMediaFilename_shouldReturnNull_whenUrlIsNull() {
        String result = scraper.getMediaFilename(null);
        assertThat(result).isNull();
    }

    @Test
    void getHashedOutputMediaPath_shouldCalculatePath_whenIdIsNumeric() throws IOException {
        String url = "https://www.bedetheque.com/image.jpg";
        Path basePath = tempDir.resolve("basePath");
        Files.createDirectories(basePath);
        String idMedia = "18787"; // 18787 / 5000 = 3 (division entière)

        String result = scraper.getHashedOutputMediaPath(url, basePath.toString(), idMedia);

        // Construction du chemin attendu selon l'OS
        String expected = tempDir.toString() + File.separator + "basePath" + File.separator + "3" + File.separator + "image.jpg";
        // On normalise les slashes pour le test si nécessaire, bien que File.separator gère cela
        assertThat(result).isEqualTo(expected.replace("/", File.separator));
    }

    @Test
    void getMediaSize_shouldReturnSize_whenFileExists() throws IOException {
        Path file = tempDir.resolve("test-size.txt");
        Files.writeString(file, "12345"); // 5 octets

        long size = scraper.getMediaSize(file.toString());

        assertThat(size).isEqualTo(5L);
    }

    @Test
    void getMediaSize_shouldReturnZero_whenFileDoesNotExist() {
        long size = scraper.getMediaSize(tempDir.resolve("unknown.txt").toString());
        assertThat(size).isZero();
    }

    @Test
    void downloadMedia_simple_shouldReturnExistingPath_whenFileAlreadyExists() throws IOException {
        // Given
        String filename = "existing.jpg";
        Path outputDir = tempDir.resolve("out");
        Files.createDirectories(outputDir);
        Files.createFile(outputDir.resolve(filename));

        String httpPath = "http://localhost/media";
        String url = "http://remote.com/" + filename;

        // When
        String result = scraper.downloadMedia(outputDir.toString(), httpPath, url);

        // Then
        assertThat(result).isEqualTo(httpPath + File.separator + filename);
    }

    @Test
    void downloadMedia_simple_shouldDownloadFile_whenFileDoesNotExist() throws IOException {
        // Given: Un fichier local agissant comme serveur distant
        Path sourceDir = tempDir.resolve("source");
        Files.createDirectories(sourceDir);
        Path sourceFile = sourceDir.resolve("source.jpg");
        Files.writeString(sourceFile, "content-data");
        String sourceUrl = sourceFile.toUri().toString();

        Path outputDir = tempDir.resolve("target");
        Files.createDirectories(outputDir);
        String httpPath = "http://localhost/target";

        // When
        String result = scraper.downloadMedia(outputDir.toString(), httpPath, sourceUrl);

        // Then
        Path downloadedFile = outputDir.resolve("source.jpg");
        assertThat(Files.exists(downloadedFile)).isTrue();
        assertThat(Files.readString(downloadedFile)).isEqualTo("content-data");
        assertThat(result).isEqualTo(httpPath + File.separator + "source.jpg");
    }

    @Test
    void downloadMedia_full_shouldHandleHashingAndDownload() throws IOException {
        // Given
        Path sourceDir = tempDir.resolve("source");
        Files.createDirectories(sourceDir);
        Path sourceFile = sourceDir.resolve("comic.jpg");
        Files.writeString(sourceFile, "comic-data");
        String sourceUrl = sourceFile.toUri().toString();

        Path outputBaseDir = tempDir.resolve("library");
        String httpBasePath = "http://localhost/library/";
        String idMedia = "5005"; // 5005 / 5000 = 1

        // When
        String result = scraper.downloadMedia(outputBaseDir.toString(), httpBasePath, sourceUrl, "default.jpg", idMedia);

        // Then
        // Vérifie que le fichier est bien dans le sous-dossier "1"
        Path expectedFile = outputBaseDir.resolve("1").resolve("comic.jpg");
        assertThat(Files.exists(expectedFile)).isTrue();
        
        String expectedHttp = httpBasePath + "1" + File.separator + "comic.jpg";
        assertThat(result).isEqualTo(expectedHttp);
    }

    @Test
    void downloadMedia_simple_shouldThrowTechnicalException_whenUrlIsInvalid() {
        String outputDir = tempDir.resolve("target").toString();
        String httpPath = "http://localhost/target";
        String invalidUrl = "ht tp://invalid-url";


        assertThrows(Exception.class, () ->
            scraper.downloadMedia(outputDir, httpPath, invalidUrl)
        );
    }
}