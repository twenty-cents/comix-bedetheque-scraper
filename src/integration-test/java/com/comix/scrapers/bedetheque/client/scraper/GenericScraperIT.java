package com.comix.scrapers.bedetheque.client.scraper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(initializers = ConfigDataApplicationContextInitializer.class)
class GenericScraperIT {

    @Value("${application.downloads.authors.photo.thumbs}")
    private String authorThumbsDir;

    @Value("${application.http.medias.authors.photo.thumbs}")
    private String httpMediaPath;

    @DisplayName("Download Media -> Ok")
    @Test
    void downloadMediaShouldReturnOk() {
        GenericScraper genericScraper = new GenericScraper();
        // Get project base directory
        String userDirectory = Path.of("")
                .toAbsolutePath()
                .toString();
        String mediaFilename = genericScraper.downloadMedia(
                userDirectory + authorThumbsDir,
                httpMediaPath,
                "https://www.bedetheque.com/media/Photos/Photo_77.jpg");
        assertThat(mediaFilename).isEqualTo("http://localhost:8080/authors/photo/thumbs/Photo_77.jpg");
    }

}