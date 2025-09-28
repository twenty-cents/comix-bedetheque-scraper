package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.client.model.rating.Rating;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(initializers = ConfigDataApplicationContextInitializer.class)
class RatingScraperIT {

    @Value("#{new Long('${application.scraping.latency}')}")
    private Long latency;

    private RatingScraper ratingScraper;

    @BeforeEach
    void setup() {
        ratingScraper = new RatingScraper();
        ratingScraper.setLocalCacheActive(false);
        ratingScraper.setLatency(latency);
    }

    @Test
    @DisplayName("Scrap all ratings of the serie Asterix")
    void scrapRatingsSerie() {
        List<Rating> ratings = ratingScraper.scrap("https://www.bedetheque.com/avis-59-BD-Asterix.html");
        // Verify
        assertThat(ratings).isNotEmpty();
        for(Rating r : ratings) {
            if(r.getGraphicNovelUrl().equals("https://www.bedetheque.com/BD-Asterix-Tome-2-La-serpe-d-or-22942.html")
            && r.getCreateBy().equals("crazybuyer1")) {
                assertThat(r.getGraphicNovelTitle()).isEqualTo("Tome 2. La serpe d'or");
                assertThat(r.getGraphicNovelUrl()).isEqualTo("https://www.bedetheque.com/BD-Asterix-Tome-2-La-serpe-d-or-22942.html");
                assertThat(r.getGraphicNovelPictureUrl()).isEqualTo("https://www.bedetheque.com/cache/thb_couv/asterix02eo_22942.jpg");
                assertThat(r.getGraphicNovelPictureTitle()).isEqualTo("Tome 2 -  La serpe d'or");
                assertThat(r.getCreateBy()).isEqualTo("crazybuyer1");
                assertThat(r.getCreateByAllRatingsUrl()).isEqualTo("https://www.bedetheque.com/avis?u=crazybuyer1");
                assertThat(r.getCreateOn()).isEqualTo("Le 09/12/2020 à 11:51:30");
                assertThat(r.getRatingPictureUrl()).isEqualTo("https://www.bdgest.com/skin/stars2/5.png");
                assertThat(r.getRatingTitle()).isEqualTo("Note : 5/5");
                assertThat(r.getComment()).startsWith("Je me dois de prendre ");
            }
        }
    }

}