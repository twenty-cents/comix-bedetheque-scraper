package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.client.model.graphicnovel.AuthorRole;
import com.comix.scrapers.bedetheque.client.model.graphicnovel.GraphicNovel;
import com.comix.scrapers.bedetheque.client.model.graphicnovel.GraphicNovelSideListItem;
import com.comix.scrapers.bedetheque.client.model.serie.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(initializers = ConfigDataApplicationContextInitializer.class)
class SerieScraperIT {

    @Value("${application.downloads.localcache.active}")
    private boolean isLocalCacheActive;

    @Value("${bedetheque.url.series.index-by-letter}")
    private String bedethequeSerieIndexByLetterUrl;

    @Value("${bedetheque.url.serie.prefix}")
    private String bedethequeSeriePrefixUrl;

    @Value("${application.http.medias.series.cover-front.thumbs}")
    private String httpCoverFrontThumbDirectory;

    @Value("${application.http.medias.series.page-example.thumbs}")
    private String httpPageExampleThumbDirectory;

    @Value("${application.http.medias.default.unavailable}")
    private String httpDefaultMediaFilename;

    @Value("${application.http.medias.series.page-example.hd}")
    private String httpPageExampleHdDirectory;

    @Value("#{new Long('${application.scraping.latency}')}")
    private Long latency;

    @Value("${application.downloads.localcache.hashed-directory-step:5000}")
    private int hashedDirectoryStep;

    @TempDir
    Path tempDir;

    private SerieScraper serieScraper;

    @BeforeEach
    void setup() {
        GraphicNovelScraper graphicNovelScraper = new GraphicNovelScraper();
        graphicNovelScraper.setLocalCacheActive(false);

        graphicNovelScraper.setLocalCacheActive(isLocalCacheActive);
        graphicNovelScraper.setLatency(latency);

        ReflectionTestUtils.setField(graphicNovelScraper, "httpCoverFrontHdDirectory", "http://localhost:8080/media/graphic-novels/cover-front/hd/");
        ReflectionTestUtils.setField(graphicNovelScraper, "httpCoverFrontThumbDirectory", "http://localhost:8080/media/graphic-novels/cover-front/thb/");
        ReflectionTestUtils.setField(graphicNovelScraper, "httpCoverBackHdDirectory", "http://localhost:8080/media/graphic-novels/cover-back/hd/");
        ReflectionTestUtils.setField(graphicNovelScraper, "httpCoverBackThumbDirectory", "http://localhost:8080/media/graphic-novels/cover-back/thb/");
        ReflectionTestUtils.setField(graphicNovelScraper, "httpPageExampleHdDirectory", "http://localhost:8080/media/graphic-novels/page-example/hd/");
        ReflectionTestUtils.setField(graphicNovelScraper, "httpPageExampleThumbDirectory", "http://localhost:8080/media/graphic-novels/page-example/thb/");
        ReflectionTestUtils.setField(graphicNovelScraper, "httpDefaultMediaFilename", "default.jpg");

        String outputCoverFrontHdDirectory = tempDir.resolve("graphic-novels/cover-front/hd").toString();
        String outputCoverFrontThumbnailDirectory = tempDir.resolve("graphic-novels/cover-front/thb").toString();
        String outputBackCoverHdDirectory = tempDir.resolve("graphic-novels/cover-back/hd").toString();
        String outputBackCoverThumbnailDirectory = tempDir.resolve("graphic-novels/cover-back/thb").toString();
        String outputPageExampleHdDirectory = tempDir.resolve("graphic-novels/page-example/hd").toString();
        String outputPageExampleThumbnailDirectory = tempDir.resolve("graphic-novels/page-example/thb").toString();

        ReflectionTestUtils.setField(graphicNovelScraper, "outputCoverFrontHdDirectory", outputCoverFrontHdDirectory);
        ReflectionTestUtils.setField(graphicNovelScraper, "outputCoverFrontThumbDirectory", outputCoverFrontThumbnailDirectory);
        ReflectionTestUtils.setField(graphicNovelScraper, "outputCoverBackThumbDirectory", outputBackCoverHdDirectory);
        ReflectionTestUtils.setField(graphicNovelScraper, "outputCoverBackHdDirectory", outputBackCoverThumbnailDirectory);
        ReflectionTestUtils.setField(graphicNovelScraper, "outputPageExampleHdDirectory", outputPageExampleHdDirectory);
        ReflectionTestUtils.setField(graphicNovelScraper, "outputPageExampleThumbDirectory", outputPageExampleThumbnailDirectory);
        ReflectionTestUtils.setField(graphicNovelScraper, "hashedDirectoryStep", 5000);

        serieScraper = new SerieScraper(graphicNovelScraper);
        serieScraper.setLocalCacheActive(isLocalCacheActive);
        serieScraper.setBedethequeSerieIndexByLetterUrl(bedethequeSerieIndexByLetterUrl);
        serieScraper.setBedethequeSeriePrefixUrl(bedethequeSeriePrefixUrl);
        serieScraper.setLatency(latency);

        ReflectionTestUtils.setField(serieScraper, "hashedDirectoryStep", hashedDirectoryStep);
        ReflectionTestUtils.setField(serieScraper, "outputCoverFrontThumbDirectory", tempDir.resolve("media/serie/cover-front/thb").toString());
        ReflectionTestUtils.setField(serieScraper, "outputPageExampleThumbDirectory", tempDir.resolve("media/serie/page-example/thb").toString());
        ReflectionTestUtils.setField(serieScraper, "outputPageExampleHdDirectory", tempDir.resolve("media/serie/page-example/hd").toString());
        ReflectionTestUtils.setField(serieScraper, "httpCoverFrontThumbDirectory", httpCoverFrontThumbDirectory);
        ReflectionTestUtils.setField(serieScraper, "httpPageExampleThumbDirectory", httpPageExampleThumbDirectory);
        ReflectionTestUtils.setField(serieScraper, "httpPageExampleHdDirectory", httpPageExampleHdDirectory);
        ReflectionTestUtils.setField(serieScraper, "httpDefaultMediaFilename", httpDefaultMediaFilename);
    }

    @Test
    @DisplayName("Get all series starting with C")
    void listByLetter() {
        // Scrap
        List<Serie> series = serieScraper.listByLetter("C");
        // Verify
        assertThat(series).hasSizeGreaterThan(4400);
        Serie s = series.getFirst();
        assertThat(s.getId()).isEqualTo("14757");
        assertThat(s.getName()).isEqualTo("C - ©");
        assertThat(s.getUrl()).isEqualTo("https://www.bedetheque.com/serie-14757-BD-C.html");
    }

    @Test
    @DisplayName("Get the urls of all series indexes")
    void listAllSeriesIndexes() {
        List<SeriesByLetter> seriesByLetters = serieScraper.listAllSeriesIndexes();
        // Verify
        assertThat(seriesByLetters).hasSize(27);
    }

    @Test
    @DisplayName("Scrap a serie one shot - Douglas Ferblanc et Vaseline agents spatiaux-spéciaux")
    void scrapOneShot() {
        SerieDetails s = serieScraper.scrap("https://www.bedetheque.com/serie-6637-BD-Douglas-Ferblanc-et-Vaseline-agents-spatiaux-speciaux.html");
        // Verify serie infos
        assertThat(s.getTitle()).isEqualTo("Douglas Ferblanc et Vaseline agents spatiaux-spéciaux");
        assertThat(s.getExternalId()).isEqualTo("6637");
        assertThat(s.getCategory()).isEqualTo("Humour");
        assertThat(s.getStatus()).isEqualTo("One shot");
        assertThat(s.getOrigin()).isEqualTo("Europe");
        assertThat(s.getGraphicNovelCount()).isEqualTo(1);
        assertThat(s.getLanguage()).isEqualTo("Français");
        assertThat(s.getPeriod()).isEqualTo("1982");
        assertThat(s.getNextSerie().getId()).isEqualTo("47626");
        assertThat(s.getPreviousSerie().getId()).isEqualTo("2661");
        assertThat(s.getTomeCount()).isEqualTo(1);
        assertThat(s.getSynopsys()).isNull();
        assertThat(s.getGraphicNovelSideList()).isEmpty();
        assertThat(s.getPageExampleThumbnailUrl()).isEqualTo("http://localhost:8080/series/page-example/thumbs/1/Bienvenueauxterriens_06062003.jpg");
        assertThat(s.getPageExampleUrl()).isEqualTo("http://localhost:8080/series/page-example/hd/1/Bienvenueauxterriens_06062003.jpg");
        assertThat(s.getRatings().getCount()).isZero();
        assertThat(s.getRatings().getUrl()).isEqualTo("https://www.bedetheque.com/avis-6637-BD-Douglas-Ferblanc-et-Vaseline-agents-spatiaux-speciaux.html");
        // Verify associated series
        assertThat(s.getLinkedSeries()).isEmpty();
        // Verify other series to read
        assertThat(s.getToReadSeries()).isEmpty();
    }

    @Test
    @DisplayName("Scrap the serie Akim, with multiples pages and linked series")
    void scrapWithMultipleGraphicNovelPages() { //NOSONAR
        SerieDetails s = serieScraper.scrap("https://www.bedetheque.com/serie-13266-BD-Akim-1re-serie.html");
        // Verify serie infos
        assertThat(s.getTitle()).isEqualTo("Akim (1re série - Aventures et Voyages)");
        assertThat(s.getExternalId()).isEqualTo("13266");
        assertThat(s.getCategory()).isEqualTo("Aventure");
        assertThat(s.getStatus()).isEqualTo("Série finie");
        assertThat(s.getOrigin()).isEqualTo("Europe");
        assertThat(s.getGraphicNovelCount()).isGreaterThanOrEqualTo(846);
        assertThat(s.getLanguage()).isEqualTo("Français");
        assertThat(s.getPeriod()).isEqualTo("1958-1991");
        assertThat(s.getNextSerie().getId()).isEqualTo("9963");
        assertThat(s.getPreviousSerie().getId()).isEqualTo("59125");
        assertThat(s.getTomeCount()).isGreaterThanOrEqualTo(756);
        assertThat(s.getSynopsys()).startsWith("- 756 Numéros");
        assertThat(s.getGraphicNovelSideList()).hasSizeGreaterThanOrEqualTo(846);
        assertThat(s.getPageExampleThumbnailUrl()).isEqualTo("http://localhost:8080/series/page-example/thumbs/2/PlancheS_13266.jpg");
        assertThat(s.getPageExampleUrl()).isEqualTo("http://localhost:8080/series/page-example/hd/2/PlancheS_13266.jpg");
        assertThat(s.getRatings().getCount()).isZero();
        assertThat(s.getRatings().getUrl()).isEqualTo("https://www.bedetheque.com/avis-13266-BD-Akim-1re-serie-Aventures-et-Voyages.html");
        // Verify associated series
        assertThat(s.getLinkedSeries()).hasSizeGreaterThanOrEqualTo(4);
        for(LinkedSerie ls : s.getLinkedSeries()) {
            if(ls.getExternalId().equals("18198")) {
                assertThat(ls.getExternalId()).isEqualTo("18198");
                assertThat(ls.getTitle()).isEqualTo("Akim-Color");
                assertThat(ls.getUrl()).isEqualTo("https://www.bedetheque.com/serie-18198-BD-Akim-Color.html");
            }
        }
        // Verify other series to read
        assertThat(s.getToReadSeries()).hasSizeGreaterThanOrEqualTo(2);
        for(ToReadSerie ts : s.getToReadSeries()) {
            if(ts.getExternalId().equals("7389")) {
                assertThat(ts.getExternalId()).isEqualTo("7389");
                assertThat(ts.getTitle()).isEqualTo("Tarzan (Editions Azur)");
                assertThat(ts.getUrl()).isEqualTo("https://www.bedetheque.com/serie-7389-BD-Tarzan-Editions-Azur.html");
                assertThat(ts.getCoverUrl()).isEqualTo("http://localhost:8080/series/cover-front/thumbs/1/Couv_32506.jpg");
                assertThat(ts.getCoverTitle()).isEqualTo("Tarzan (Editions Azur)");
            }
        }
        // Verify graphic novel side list
        for(GraphicNovelSideListItem g : s.getGraphicNovelSideList()) {
            if(g.getExternalId().equals("246591")) {
                assertThat(g.getExternalId()).isEqualTo("246591");
                assertThat(g.getTome()).isEmpty();
                assertThat(g.getNumEdition()).isEqualTo("Rec178");
                assertThat(g.getTitle()).isEqualTo("Album N°178 (n°701, 702, 703 et 705)");
                assertThat(g.getPublicationDate()).isEqualTo("01/1989");
                assertThat(g.getUrl()).isEqualTo("https://www.bedetheque.com/BD-Akim-1re-serie-Aventures-et-Voyages-Rec178-Album-N178-n701-702-703-et-705-246591.html");
            }
        }
    }

    @Test
    @DisplayName("Scrap the serie Asterix, with all graphic novels in one page")
    void scrapWithAllGraphicNovelsInOnePage() { //NOSONAR
        SerieDetails s = serieScraper.scrap("https://www.bedetheque.com/serie-59-BD-Asterix__10000.html");
        // Verify serie infos
        assertThat(s.getTitle()).isEqualTo("Astérix");
        assertThat(s.getExternalId()).isEqualTo("59");
        assertThat(s.getCategory()).isEqualTo("Humour");
        assertThat(s.getStatus()).isEqualTo("Série en cours");
        assertThat(s.getOrigin()).isEqualTo("Europe");
        assertThat(s.getGraphicNovelCount()).isGreaterThanOrEqualTo(38);
        assertThat(s.getLanguage()).isEqualTo("Français");
        assertThat(s.getPeriod()).startsWith("1961-20");
        assertThat(s.getNextSerie().getId()).isEqualTo("75046");
        assertThat(s.getPreviousSerie().getId()).isEqualTo("80266");
        assertThat(s.getTomeCount()).isGreaterThanOrEqualTo(38);
        assertThat(s.getSynopsys()).startsWith("Cette série regroupe");
        assertThat(s.getGraphicNovelSideList()).hasSizeGreaterThanOrEqualTo(38);
        assertThat(s.getPageExampleThumbnailUrl()).isEqualTo("http://localhost:8080/series/page-example/thumbs/0/PlancheS_59.jpg");
        assertThat(s.getPageExampleUrl()).isEqualTo("http://localhost:8080/series/page-example/hd/0/PlancheS_59.jpg");
        assertThat(s.getCopyright()).isNotBlank();
        assertThat(s.getRatings().getCount()).isGreaterThan(389);
        assertThat(s.getRatings().getUrl()).isEqualTo("https://www.bedetheque.com/avis-59-BD-Asterix.html");
        // Verify associated series
        assertThat(s.getLinkedSeries()).hasSize(3);
        // Verify other series to read
        assertThat(s.getToReadSeries()).hasSize(14);
        for(ToReadSerie ts : s.getToReadSeries()) {
            if (ts.getExternalId().equals("36874")) {
                assertThat(ts.getExternalId()).isEqualTo("36874");
                assertThat(ts.getTitle()).isEqualTo("Astérix (Collection Atlas - Les archives)");
                assertThat(ts.getUrl()).isEqualTo("https://www.bedetheque.com/serie-36874-BD-Asterix-Collection-Atlas-Les-archives.html");
                assertThat(ts.getCoverUrl()).isNotBlank();
                assertThat(ts.getCoverTitle()).isEqualTo("Astérix (Collection Atlas - Les archives)");
            }
        }
        // Verify graphic novel side list
        for(GraphicNovelSideListItem g : s.getGraphicNovelSideList()) {
            if (g.getExternalId().equals("106")) {
                assertThat(g.getExternalId()).isEqualTo("106");
                assertThat(g.getTome()).isEqualTo("32");
                assertThat(g.getNumEdition()).isEqualTo("Pub");
                assertThat(g.getTitle()).isEqualTo("Astérix et la rentrée gauloise");
                assertThat(g.getPublicationDate()).isEqualTo("01/1993");
                assertThat(g.getUrl()).isEqualTo("https://www.bedetheque.com/BD-Asterix-Tome-32Pub-Asterix-et-la-rentree-gauloise-106.html");
            }
        }
    }

    @Test
    @DisplayName("Scrap the serie Asterix, with all graphic novels in page 3")
    void scrapGraphicNovelsInPage() {
        SerieDetails s = serieScraper.scrap("https://www.bedetheque.com/serie-13266-BD-Akim-1re-serie__2.html");
        assertThat(s.getGraphicNovels()).hasSize(10);
    }

    @Test
    @DisplayName("Scrap all graphic novels of Asterix in one page")
    void retrieveAlbums() { //NOSONAR
        List<GraphicNovel> sc = serieScraper.scrapGraphicNovels("https://www.bedetheque.com/serie-59-BD-Asterix__10000.html");
        // Test
        assertThat(sc).hasSizeGreaterThanOrEqualTo(38);
        for(GraphicNovel g : sc) {
            if(g.getExternalId().equals("375114")) {
                assertThat(g.getExternalId()).isEqualTo("375114");
                assertThat(g.getTome()).isEqualTo("38");
                assertThat(g.getNumEdition()).isNull();
                assertThat(g.getTitle()).isEqualTo("La Fille de Vercingétorix");
                assertThat(g.getPublicationDate()).isEqualTo("10/2019");
                assertThat(g.getReleaseDate()).isEqualTo("24/10/2019");
                assertThat(g.getPublisher()).isEqualTo("Les Éditions Albert René");
                assertThat(g.getCollection()).isNull();
                assertThat(g.getIsbn()).isEqualTo("9782864973423");
                assertThat(g.getTotalPages()).isEqualTo(44);
                assertThat(g.getFormat()).isEqualTo("Format normal");
                assertThat(g.getIsOriginalPublication()).isTrue();
                assertThat(g.getIsIntegrale()).isFalse();
                assertThat(g.getIsBroche()).isFalse();
                assertThat(g.getInfoEdition()).startsWith("Noté");
                assertThat(g.getReeditionUrl()).isEqualTo("https://www.bedetheque.com/BD-Asterix-Tome-38-La-Fille-de-Vercingetorix-375114.html#reed");
                assertThat(g.getExternalIdOriginalPublication()).isNull();
                assertThat(g.getCoverThumbnailUrl()).isEqualTo("http://localhost:8080/media/graphic-novels/cover-front/thb/75/Couv_375114.jpg");
                assertThat(g.getBackCoverOriginalUrl()).isEqualTo("https://www.bedetheque.com/media/Versos/Verso_375114.jpg");
                assertThat(g.getBackCoverThumbnailOriginalUrl()).isEqualTo("https://www.bedetheque.com/cache/thb_versos/Verso_375114.jpg");
                assertThat(g.getPageExampleOriginalUrl()).isEqualTo("https://www.bedetheque.com/media/Planches/PlancheA_375114.jpg");
                assertThat(g.getPageExampleThumbnailOriginalUrl()).isEqualTo("https://www.bedetheque.com/cache/thb_planches/PlancheA_375114.jpg");

                // Verify authors
                assertThat(g.getAuthors()).hasSize(3);
                for(AuthorRole r : g.getAuthors()) {
                    if(r.getExternalId().equals("6227")) {
                        assertThat(r.getExternalId()).isEqualTo("6227");
                        assertThat(r.getRole()).isEqualTo("Scénario");
                        assertThat(r.getName()).isEqualTo("Ferri, Jean-Yves");
                        assertThat(r.getAuthorUrl()).isEqualTo("https://www.bedetheque.com/auteur-6227-BD-Ferri-Jean-Yves.html");
                    }
                }
            }
        }
    }

}