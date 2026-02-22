package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.client.model.media.Media;
import com.comix.scrapers.bedetheque.client.model.media.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaScraperTest {

    @InjectMocks
    private MediaScraper mediaScraper;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Series
        String serieOutputPageExampleThumbDirectory = tempDir.resolve("serie/page-example/thumbs").toString();
        String serieOutputPageExampleHdDirectory = tempDir.resolve("serie/page-example/hd").toString();
        String serieOutputCoverFrontThumbDirectory = tempDir.resolve("serie/cover/thumbs").toString();

        // Comic books
        String comicBookOutputCoverFrontHdDirectory = tempDir.resolve("graphic-novels/cover-front/hd").toString();
        String comicBookOutputCoverFrontThumbnailDirectory = tempDir.resolve("graphic-novels/cover-front/thumbs").toString();
        String comicBookOutputBackCoverHdDirectory = tempDir.resolve("graphic-novels/cover-back/hd").toString();
        String comicBookOutputBackCoverThumbnailDirectory = tempDir.resolve("graphic-novels/cover-back/thumbs").toString();
        String comicBookOutputPageExampleHdDirectory = tempDir.resolve("graphic-novels/page-example/hd").toString();
        String comicBookOutputPageExampleThumbnailDirectory = tempDir.resolve("graphic-novels/page-example/thumbs").toString();

        // Authors
        String authorOutputAuthorHdDirectory = tempDir.resolve("authors/photo/hd").toString();

        // Inject media paths
        // Series
        ReflectionTestUtils.setField(mediaScraper, "serieOutputPageExampleThumbDirectory", serieOutputPageExampleThumbDirectory);
        ReflectionTestUtils.setField(mediaScraper, "serieHttpPageExampleThumbDirectory", "http://localhost:8080/media/serie/page-example/thumbs/");
        ReflectionTestUtils.setField(mediaScraper, "serieOutputPageExampleHdDirectory", serieOutputPageExampleHdDirectory);
        ReflectionTestUtils.setField(mediaScraper, "serieHttpPageExampleHdDirectory", "http://localhost:8080/media/serie/page-example/hd/");
        ReflectionTestUtils.setField(mediaScraper, "serieOutputCoverFrontThumbDirectory", serieOutputCoverFrontThumbDirectory);
        ReflectionTestUtils.setField(mediaScraper, "serieHttpCoverFrontThumbDirectory", "http://localhost:8080/media/serie/cover/thumbs/");
        // Comic books
        ReflectionTestUtils.setField(mediaScraper, "comicBookHttpCoverFrontHdDirectory", "http://localhost:8080/media/graphic-novels/cover-front/hd/");
        ReflectionTestUtils.setField(mediaScraper, "comicBookHttpCoverFrontThumbDirectory", "http://localhost:8080/media/graphic-novels/cover-front/thumbs/");
        ReflectionTestUtils.setField(mediaScraper, "comicBookHttpCoverBackHdDirectory", "http://localhost:8080/media/graphic-novels/cover-back/hd/");
        ReflectionTestUtils.setField(mediaScraper, "comicBookHttpCoverBackThumbDirectory", "http://localhost:8080/media/graphic-novels/cover-back/thumbs/");
        ReflectionTestUtils.setField(mediaScraper, "comicBookHttpPageExampleHdDirectory", "http://localhost:8080/media/graphic-novels/page-example/hd/");
        ReflectionTestUtils.setField(mediaScraper, "comicBookHttpPageExampleThumbDirectory", "http://localhost:8080/media/graphic-novels/page-example/thumbs/");

        ReflectionTestUtils.setField(mediaScraper, "comicBookOutputCoverFrontHdDirectory", comicBookOutputCoverFrontHdDirectory);
        ReflectionTestUtils.setField(mediaScraper, "comicBookOutputCoverFrontThumbDirectory", comicBookOutputCoverFrontThumbnailDirectory);
        ReflectionTestUtils.setField(mediaScraper, "comicBookOutputCoverBackThumbDirectory", comicBookOutputBackCoverThumbnailDirectory);
        ReflectionTestUtils.setField(mediaScraper, "comicBookOutputCoverBackHdDirectory", comicBookOutputBackCoverHdDirectory);
        ReflectionTestUtils.setField(mediaScraper, "comicBookOutputPageExampleHdDirectory", comicBookOutputPageExampleHdDirectory);
        ReflectionTestUtils.setField(mediaScraper, "comicBookOutputPageExampleThumbDirectory", comicBookOutputPageExampleThumbnailDirectory);

        // Authors
        ReflectionTestUtils.setField(mediaScraper, "authorOutputAuthorHdDirectory", authorOutputAuthorHdDirectory);
        ReflectionTestUtils.setField(mediaScraper, "authorHttpAuthorHdPath", "http://localhost:8080/media/authors/photo/hd");

        ReflectionTestUtils.setField(mediaScraper, "hashedDirectoryStep", 5000);
    }

    @Test
    @DisplayName("Should scrap serie example page")
    void shouldScrapSerieExamplePage() {
        scrapMedia(
                "18732",
                MediaType.SERIE_EXAMPLE_PAGE,
                "https://www.bedetheque.com/media/Couvertures/spirouetfantasio01_18732.jpg",
                "http://localhost:8080/media/serie/page-example/hd/3/spirouetfantasio01_18732.jpg",
                "serie/page-example/hd/3/spirouetfantasio01_18732.jpg",
                "spirouetfantasio01_18732.jpg"
        );
    }

    @Test
    @DisplayName("Should scrap serie example page thumbnail")
    void shouldScrapSerieExamplePageThumbnail() {
        scrapMedia(
                "18732",
                MediaType.SERIE_EXAMPLE_PAGE_THUMBNAIL,
                "https://www.bedetheque.com/cache/thb_planches/PlancheA_18732.jpg",
                "http://localhost:8080/media/serie/page-example/thumbs/3/PlancheA_18732.jpg",
                "serie/page-example/thumbs/3/PlancheA_18732.jpg",
                "PlancheA_18732.jpg"
        );
    }

    @Test
    @DisplayName("Should scrap serie cover")
    void shouldScrapSerieCoverThumbnail() {
        scrapMedia(
                "13",
                MediaType.SERIE_COVER_THUMBNAIL,
                "https://www.bedetheque.com/cache/thb_couv/AchilleTalon_11_13.jpg",
                "http://localhost:8080/media/serie/cover/thumbs/0/AchilleTalon_11_13.jpg",
                "serie/cover/thumbs/0/AchilleTalon_11_13.jpg",
                "AchilleTalon_11_13.jpg"
        );
    }

    @Test
    @DisplayName("Should scrap comic book cover")
    void shouldScrapComicBookCover() {
        scrapMedia(
                "18732",
                MediaType.COMIC_BOOK_COVER,
                "https://www.bedetheque.com/media/Couvertures/spirouetfantasio01_18732.jpg",
                "http://localhost:8080/media/graphic-novels/cover-front/hd/3/spirouetfantasio01_18732.jpg",
                "graphic-novels/cover-front/hd/3/spirouetfantasio01_18732.jpg",
                "spirouetfantasio01_18732.jpg"
        );
    }

    @Test
    @DisplayName("Should scrap comic book cover thumbnail")
    void shouldScrapComicBookCoverThumbnail() {
        scrapMedia(
                "18732",
                MediaType.COMIC_BOOK_COVER_THUMBNAIL,
                "https://www.bedetheque.com/cache/thb_couv/spirouetfantasio01_18732.jpg",
                "http://localhost:8080/media/graphic-novels/cover-front/thumbs/3/spirouetfantasio01_18732.jpg",
                "graphic-novels/cover-front/thumbs/3/spirouetfantasio01_18732.jpg",
                "spirouetfantasio01_18732.jpg"
        );
    }

    @Test
    @DisplayName("Should scrap comic book back cover")
    void shouldScrapComicBookBackCover() {
        scrapMedia(
                "18732",
                MediaType.COMIC_BOOK_BACKCOVER,
                "https://www.bedetheque.com/media/Versos/spirouetfantasio01v_18732.jpg",
                "http://localhost:8080/media/graphic-novels/cover-back/hd/3/spirouetfantasio01v_18732.jpg",
                "graphic-novels/cover-back/hd/3/spirouetfantasio01v_18732.jpg",
                "spirouetfantasio01v_18732.jpg"
        );
    }

    @Test
    @DisplayName("Should scrap comic book back cover thumbnail")
    void shouldScrapComicBookBackCoverThumbnail() {
        scrapMedia(
                "18732",
                MediaType.COMIC_BOOK_BACKCOVER_THUMBNAIL,
                "https://www.bedetheque.com/cache/thb_versos/spirouetfantasio01v_18732.jpg",
                "http://localhost:8080/media/graphic-novels/cover-back/thumbs/3/spirouetfantasio01v_18732.jpg",
                "graphic-novels/cover-back/thumbs/3/spirouetfantasio01v_18732.jpg",
                "spirouetfantasio01v_18732.jpg"
        );
    }

    @Test
    @DisplayName("Should scrap comic book example page")
    void shouldScrapComicBookExamplePage() {
        scrapMedia(
                "18732",
                MediaType.COMIC_BOOK_EXAMPLE_PAGE,
                "https://www.bedetheque.com/media/Planches/PlancheA_18732.jpg",
                "http://localhost:8080/media/graphic-novels/page-example/hd/3/PlancheA_18732.jpg",
                "graphic-novels/page-example/hd/3/PlancheA_18732.jpg",
                "PlancheA_18732.jpg"
        );
    }

    @Test
    @DisplayName("Should scrap comic book example page thumbnail")
    void shouldScrapComicBookExamplePageThumbnail() {
        scrapMedia(
                "18732",
                MediaType.COMIC_BOOK_EXAMPLE_PAGE_THUMBNAIL,
                "https://www.bedetheque.com/cache/thb_planches/PlancheA_18732.jpg",
                "http://localhost:8080/media/graphic-novels/page-example/thumbs/3/PlancheA_18732.jpg",
                "graphic-novels/page-example/thumbs/3/PlancheA_18732.jpg",
                "PlancheA_18732.jpg"
        );
    }

    @Test
    @DisplayName("Should scrap author photo")
    void shouldScrapAuthorPhoto() {
        scrapMedia(
                "63",
                MediaType.AUTHOR_PHOTO,
                "https://www.bedetheque.com/media/Photos/Photo_63.jpg",
                "http://localhost:8080/media/authors/photo/hd/0/Photo_63.jpg",
                "authors/photo/hd/0/Photo_63.jpg",
                "Photo_63.jpg"
        );
    }

    private void scrapMedia(String id, MediaType type, String originalUrl, String expectedUrl, String expectedPath, String expectedFilename) {
        MediaScraper scraperSpy = spy(mediaScraper);
        lenient().doNothing().when(scraperSpy).download(anyString(), anyString());

        try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class)) {
            GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
            mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);

            Media media = scraperSpy.scrap(id, type, originalUrl);

            verify(scraperSpy, times(1)).download(any());
            assertThat(media.getId()).isEqualTo(id);
            assertThat(media.getType()).isEqualTo(type);
            assertThat(media.getOriginalUrl()).isEqualTo(originalUrl);
            assertThat(media.getUrl()).isEqualTo(expectedUrl);
            assertThat(media.getPath()).isEqualTo(tempDir.resolve(expectedPath).toString());
            assertThat(media.getFilename()).isEqualTo(expectedFilename);
            assertThat(media.getAvailable()).isTrue();
            assertThat(media.getFileSize()).isZero();
        }
    }
}