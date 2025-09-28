package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.util.HTML;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScraperTest {

    // 1. On crée une implémentation concrète et minimale de la classe abstraite pour les tests.
    private static class TestScraper extends Scraper {}

    private final Scraper scraper = new TestScraper();

    @Nested
    @DisplayName("Tests for attr(element, attributeKey)")
    class AttrTests {

        @Test
        @DisplayName("should return attribute value when element and attribute exist")
        void shouldReturnAttributeValue() {
            // GIVEN
            Element element = Jsoup.parseBodyFragment("<a href=' https://test.com '>Link</a>").selectFirst("a");

            // WHEN
            String result = scraper.attr(element, HTML.Attribute.HREF);

            // THEN
            assertThat(result).isEqualTo("https://test.com");
        }

        @Test
        @DisplayName("should return empty string when attribute does not exist")
        void shouldReturnEmptyStringForMissingAttribute() {
            // GIVEN
            Element element = Jsoup.parseBodyFragment("<a>Link</a>").selectFirst("a");

            // WHEN
            String result = scraper.attr(element, HTML.Attribute.HREF);

            // THEN
            assertThat(result).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("should return null when element is null")
        void shouldReturnNullWhenElementIsNull() {
            // WHEN
            String result = scraper.attr(null, HTML.Attribute.HREF);

            // THEN
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Tests for ownText(element)")
    class OwnTextTests {

        @Test
        @DisplayName("should return own text when element has direct text")
        void shouldReturnOwnText() {
            // GIVEN
            Element element = Jsoup.parseBodyFragment("<div> Direct Text <span>Child Text</span> </div>").selectFirst("div");

            // WHEN
            String result = scraper.ownText(element);

            // THEN
            assertThat(result).isEqualTo("Direct Text");
        }

        @Test
        @DisplayName("should return null when element has no direct text")
        void shouldReturnNullForNoOwnText() {
            // GIVEN
            Element element = Jsoup.parseBodyFragment("<div><span>Child Text</span></div>").selectFirst("div");

            // WHEN
            String result = scraper.ownText(element);

            // THEN
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should return null when element is null")
        void shouldReturnNullWhenElementIsNull() {
            // WHEN
            String result = scraper.ownText(null);

            // THEN
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Tests for text(element)")
    class TextTests {

        @Test
        @DisplayName("should return combined text of element and its children")
        void shouldReturnCombinedText() {
            // GIVEN
            Element element = Jsoup.parseBodyFragment("<div> Direct Text <span>Child Text</span> </div>").selectFirst("div");

            // WHEN
            String result = scraper.text(element);

            // THEN
            assertThat(result).isEqualTo("Direct Text Child Text");
        }

        @Test
        @DisplayName("should return trimmed text")
        void shouldReturnTrimmedText() {
            // GIVEN
            Element element = Jsoup.parseBodyFragment("<div>  Some Text  </div>").selectFirst("div");

            // WHEN
            String result = scraper.text(element);

            // THEN
            assertThat(result).isEqualTo("Some Text");
        }

        @Test
        @DisplayName("should return null when element is null")
        void shouldReturnNullWhenElementIsNull() {
            // WHEN
            String result = scraper.text(null);

            // THEN
            assertThat(result).isNull();
        }
    }
}