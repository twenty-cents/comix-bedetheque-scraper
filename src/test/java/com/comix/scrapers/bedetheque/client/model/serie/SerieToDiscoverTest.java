package com.comix.scrapers.bedetheque.client.model.serie;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SerieToDiscoverTest {

    @Test
    void compareTo_shouldSortByTitleAlphabetically() {
        // Given
        SerieToDiscover serieA = new SerieToDiscover();
        serieA.setTitle("Asterix");

        SerieToDiscover serieB = new SerieToDiscover();
        serieB.setTitle("Blacksad");

        SerieToDiscover serieAClone = new SerieToDiscover();
        serieAClone.setTitle("Asterix");

        // When & Then
        // "Asterix" vient avant "Blacksad" -> négatif
        assertThat(serieA.compareTo(serieB)).isNegative();

        // "Blacksad" vient après "Asterix" -> positif
        assertThat(serieB.compareTo(serieA)).isPositive();

        // "Asterix" est égal à "Asterix" -> zéro
        assertThat(serieA.compareTo(serieAClone)).isZero();
    }

    @Test
    void lombokMethods_shouldWorkAsExpected() {
        // Test du constructeur AllArgs et des Getters
        SerieToDiscover serie = new SerieToDiscover(
                "1", "Title", "http://url", "http://orig", "http://cover",
                "/path", "file.jpg", 100L, true, "CoverTitle"
        );

        assertThat(serie.getId()).isEqualTo("1");
        assertThat(serie.getTitle()).isEqualTo("Title");
        assertThat(serie.getUrl()).isEqualTo("http://url");
        assertThat(serie.getOriginalCoverUrl()).isEqualTo("http://orig");
        assertThat(serie.getCoverUrl()).isEqualTo("http://cover");
        assertThat(serie.getCoverPath()).isEqualTo("/path");
        assertThat(serie.getCoverFilename()).isEqualTo("file.jpg");
        assertThat(serie.getCoverSize()).isEqualTo(100L);
        assertThat(serie.getIsCoverChecked()).isTrue();
        assertThat(serie.getCoverTitle()).isEqualTo("CoverTitle");

        // Test Equals et HashCode
        SerieToDiscover sameSerie = new SerieToDiscover(
                "1", "Title", "http://url", "http://orig", "http://cover",
                "/path", "file.jpg", 100L, true, "CoverTitle"
        );
        SerieToDiscover differentSerie = new SerieToDiscover();
        differentSerie.setId("2");

        assertThat(serie).isEqualTo(sameSerie);
        assertThat(serie.hashCode()).isEqualTo(sameSerie.hashCode());
        assertThat(serie).isNotEqualTo(differentSerie);

        // Test ToString
        assertThat(serie.toString()).contains("Title", "1", "http://url");
    }

    @Test
    void noArgsConstructor_shouldCreateEmptyObject() {
        SerieToDiscover serie = new SerieToDiscover();
        assertThat(serie).isNotNull();
        assertThat(serie.getId()).isNull();
    }
}