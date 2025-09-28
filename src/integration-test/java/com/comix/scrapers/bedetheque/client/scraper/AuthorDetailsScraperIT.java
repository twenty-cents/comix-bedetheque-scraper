package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.client.model.author.Author;
import com.comix.scrapers.bedetheque.client.model.author.AuthorDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(initializers = ConfigDataApplicationContextInitializer.class)
class AuthorDetailsScraperIT {

    @Value("${bedetheque.url.authors.index-by-letter}")
    private String bedethequeAuthorsListByLetter;

    @Value("${bedetheque.url.author.prefix}")
    private String bedethequeAuthorPrefixUrl;

    @Value("${application.downloads.localcache.active}")
    private Boolean isLocalCacheActive;

    @Value("#{new Long('${application.scraping.latency}')}")
    private Long latency;

    private AuthorScraper authorScraper;

    @BeforeEach
    void setup() {
        authorScraper = new AuthorScraper();
        authorScraper.setLocalCacheActive(isLocalCacheActive);
        authorScraper.setBedethequeAuthorsListByLetter(bedethequeAuthorsListByLetter);
        authorScraper.setBedethequeAuthorPrefixUrl(bedethequeAuthorPrefixUrl);
        authorScraper.setLatency(latency);
    }

    @Test
    @DisplayName("Extract author Id from an author bedetheque url")
    void getBedethequeIdShouldReturnOk() {
        String bedethequeId = authorScraper.getBedethequeId("https://www.bedetheque.com/auteur-77-BD-Greg.html");
        assertThat(bedethequeId).contains("77");
    }

    @Test
    @DisplayName("Get index url for all authors starting with X")
    void getIndexUrlsByLetterShouldReturnOk() {
        String indexUrl = authorScraper.getIndexUrlsByLetter("X");
        assertThat(indexUrl).isEqualTo("https://www.bedetheque.com/liste_auteurs_BD_X.html");
    }

    @Test
    @DisplayName("Get a list of all authors starting with a letter (A)")
    void scrapIndexUrlsByLetterShouldReturnOk() {
        // Scrap authors list
        List<Author> scrapedAuthorList = authorScraper.scrapAuthorsIndexedByLetter("A");

        // Tests
        assertThat(scrapedAuthorList.size())
                .isPositive()
                .isGreaterThan(1700);
        assertThat(scrapedAuthorList.getFirst().getUrl()).isEqualToIgnoringCase("https://www.bedetheque.com/auteur-74561-BD-A.html");
        assertThat(scrapedAuthorList.getFirst().getName()).isEqualToIgnoringCase("A");
    }

    @Test
    @DisplayName("Get author properties (Greg)")
    void scrapAuthorShouldReturnOk() {
        // Scrap author
        AuthorDetails scrapAuthorDetails = authorScraper.scrap("https://www.bedetheque.com/auteur-77-BD-Greg.html");

        // Tests
        assertThat(scrapAuthorDetails.getId()).isEqualTo("77");
        assertThat(scrapAuthorDetails.getNickname()).isEqualTo("Greg");
        assertThat(scrapAuthorDetails.getLastname()).isEqualToIgnoringCase("regnier");
        assertThat(scrapAuthorDetails.getFirstname()).isEqualToIgnoringCase("michel");
        assertThat(scrapAuthorDetails.getBirthdate()).isEqualTo("05/05/1931");
        assertThat(scrapAuthorDetails.getDeceaseDate()).isEqualTo("29/10/1999");
        assertThat(scrapAuthorDetails.getNationality()).isEqualToIgnoringCase("belgique");
        assertThat(scrapAuthorDetails.getSiteUrl()).isNull();
        assertThat(scrapAuthorDetails.getOtherAuthorPseudonym().getId()).isEqualTo("3729");
        assertThat(scrapAuthorDetails.getOtherAuthorPseudonym().getName()).isEqualTo("Albert, Louis");
        assertThat(scrapAuthorDetails.getOtherAuthorPseudonym().getUrl()).isEqualTo("https://www.bedetheque.com/auteur-3729-BD-Albert-Louis.html");
        assertThat(scrapAuthorDetails.getPhotoUrl()).isEqualToIgnoringCase("https://www.bedetheque.com/media/Photos/Photo_77.jpg");
        assertThat(scrapAuthorDetails.getBiography()).isGreaterThan("");
        assertThat(scrapAuthorDetails.getAuthorUrl()).isEqualToIgnoringCase("https://www.bedetheque.com/auteur-77-BD-Greg.html");
        assertThat(scrapAuthorDetails.getPhotoThbUrl()).isEqualToIgnoringCase("https://www.bedetheque.com/media/Photos/Photo_77.jpg");
    }
}