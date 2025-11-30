package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.client.model.graphicnovel.GraphicNovel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(initializers = ConfigDataApplicationContextInitializer.class)
class GraphicNovelScraperIT {

    @Value("${application.downloads.localcache.active}")
    private boolean isLocalCacheActive;

    @Value("#{new Long('${application.scraping.latency}')}")
    private Long latency;

    private GraphicNovelScraper graphicNovelScraper;

    @BeforeEach
    void setup() {
        graphicNovelScraper = new GraphicNovelScraper();
        graphicNovelScraper.setLocalCacheActive(isLocalCacheActive);
        graphicNovelScraper.setLatency(latency);
    }

    @DisplayName("Scrap Astérix with all republications -> OK")
    @Test
    void scrapElementWithAllRepublicationsShouldReturnOk() {
        graphicNovelScraper.setLocalCacheActive(isLocalCacheActive);
        List<GraphicNovel> g = graphicNovelScraper.scrapWithAllRepublications("https://www.bedetheque.com/BD-Asterix-Tome-1-Asterix-le-gaulois-22940.html");
        // Verify
        assertThat(g).hasSize(26);
        GraphicNovel g0 = g.getFirst();
        assertThat(g0.getTome()).isEqualTo("1");
        assertThat(g0.getNumEdition()).isNull();
        assertThat(g0.getTitle()).isEqualTo("Astérix le Gaulois");
        assertThat(g0.getExternalId()).isEqualTo("22940");
        assertThat(g0.getPublicationDate()).isEqualTo("07/1961");
        assertThat(g0.getReleaseDate()).isNull();
        assertThat(g0.getPublisher()).isEqualTo("Dargaud");
        assertThat(g0.getCollection()).isEqualTo("La Collection Pilote");
        assertThat(g0.getCollectionUrl()).isEqualTo("https://www.bedetheque.com/search/albums?RechCollection=La+Collection+Pilote&RechEO=1");
        assertThat(g0.getIsbn()).isNull();
        assertThat(g0.getTotalPages()).isEqualTo(44);
        assertThat(g0.getIsOriginalPublication()).isTrue();
        assertThat(g0.getIsIntegrale()).isFalse();
        // Verify authors
        assertThat(g0.getAuthors().getFirst().getName()).isEqualTo("Goscinny, René");
    }

    @DisplayName("Scrap with release date -> OK")
    @Test
    void scrapWithReleaseDateShouldReturnOk() {
        String url = "https://www.bedetheque.com/serie-68754-BD-Ne-regarde-pas-derriere-toi__10000.html";
        List<GraphicNovel> sc = graphicNovelScraper.scrapFromSerie(url);
        // Test
        assertThat(sc.getFirst().getReleaseDate()).isEqualTo("21/02/2020");
    }

    @DisplayName("Scrap with publication and parution date -> OK")
    @Test
    void scrapElementWithPublicationDateAndParutionShouldReturnOk() {
        String url = "https://www.bedetheque.com/serie-67280-BD-Pauvre-humanite__10000.html";
        List<GraphicNovel> sc = graphicNovelScraper.scrapFromSerie(url);
        // Test
        assertThat(sc.getFirst().getPublicationDate()).isEqualTo("05/2019");
        assertThat(sc.getFirst().getReleaseDate()).isEqualTo("01/06/2019");
    }

    @DisplayName("Scrap with long edition number -> OK")
    @Test
    void scrapElementWithLongNumEditionShouldReturnOk() {
        String url = "https://www.bedetheque.com/serie-19097-BD-Graine-de-Pro-l-album__10000.html";
        List<GraphicNovel> sc = graphicNovelScraper.scrapFromSerie(url);
        // Test
        assertThat(sc.getFirst().getTome()).isEmpty();
        assertThat(sc.getFirst().getNumEdition()).isNull();
        assertThat(sc.getFirst().getTitle()).isEqualTo("L'album des lauréats du concours B.D scolaire 85.93");
    }

    @DisplayName("Scrap without tome -> OK")
    @Test
    void scrapWithoutTomeShouldReturnOk() {
        String url = "https://www.bedetheque.com/serie-29776-BD-Atar-Gull-ou-le-destin-d-un-esclave-modele__10000.html";
        List<GraphicNovel> sc = graphicNovelScraper.scrapFromSerie(url);
        // Test
        assertThat(sc.getFirst().getTome()).isEmpty();
    }

    @DisplayName("Scrap with quotes in title -> OK")
    @Test
    void scrapWithTitleQuotesShouldReturnOk() {
        List<GraphicNovel> sc = graphicNovelScraper.scrapFromSerie("https://www.bedetheque.com/serie-1343-BD-Et-patati-et-patata__10000.html");
        // Test
        assertThat(sc.getFirst().getTitle()).isEqualTo("\"Et patati, et patata...\"");
    }

    @DisplayName("Scrap with points int the tome -> OK")
    @Test
    void scrapWithTomeAndPointShouldReturnOk() {
        List<GraphicNovel> sc = graphicNovelScraper.scrapFromSerie("https://www.bedetheque.com/serie-29734-BD-20-sur-l-esprit-de-la-foret__10000.html");
        // Test
        assertThat(sc.getFirst().getTitle()).isEqualTo("-20% sur l'esprit de la forêt");
    }

    @DisplayName("Scrap with point in tome and no point before the title -> OK")
    @Test
    void scrapWithTomeAndPointAndTitleStartWithPointShouldReturnOk() {
        List<GraphicNovel> sc = graphicNovelScraper.scrapFromSerie("https://www.bedetheque.com/serie-57019-BD-Et-les-bebes-viennent-de-Saturne__10000.html");
        // Test
        assertThat(sc.getFirst().getTitle()).isEqualTo("... Et les bébés viennent de Saturne !");
    }

    @DisplayName("Scrap with tome -> OK")
    @Test
    void scrapWithTomeShouldReturnOk() {
        List<GraphicNovel> sc = graphicNovelScraper.scrapFromSerie("https://www.bedetheque.com/serie-41252-BD-Reversal__10000.html");
        // Test
        assertThat(sc.getFirst().getTitle()).isEqualTo("Tome 1");
    }
}