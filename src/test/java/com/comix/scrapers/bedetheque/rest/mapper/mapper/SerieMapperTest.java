package com.comix.scrapers.bedetheque.rest.mapper.mapper;

import com.comix.scrapers.bedetheque.client.model.graphicnovel.GraphicNovel;
import com.comix.scrapers.bedetheque.client.model.graphicnovel.GraphicNovelSideListItem;
import com.comix.scrapers.bedetheque.client.model.serie.*;
import com.comix.scrapers.bedetheque.rest.mapper.SerieMapper;
import com.comix.scrapers.bedetheque.rest.v1.dto.SerieDetailsDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.SerieDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.SeriesByLetterDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SerieMapperTest {

    // On récupère une instance de l'implémentation du mapper générée par MapStruct
    private final SerieMapper serieMapper = Mappers.getMapper(SerieMapper.class);

    @Test
    @DisplayName("Should map SeriesByLetter to SeriesByLetterDto correctly")
    void shouldMapSeriesByLetterToDto() {
        // GIVEN
        SeriesByLetter source = new SeriesByLetter("A", "https://a.com");

        // WHEN
        SeriesByLetterDto destination = serieMapper.seriesByLetterToSeriesByLetterDto(source);

        // THEN
        assertThat(destination).isNotNull();
        assertThat(destination.getLetter()).isEqualTo(source.getLetter());
        assertThat(destination.getUrl()).isEqualTo(source.getUrl());
    }

    @Test
    @DisplayName("Should return null when SeriesByLetter source is null")
    void shouldReturnNullForNullSeriesByLetter() {
        // WHEN
        SeriesByLetterDto destination = serieMapper.seriesByLetterToSeriesByLetterDto(null);

        // THEN
        assertThat(destination).isNull();
    }

    @Test
    @DisplayName("Should map Serie to SerieDto correctly")
    void shouldMapSerieToDto() {
        // GIVEN
        Serie source = new Serie("id", "Lanfeust de Troy", "https://lanfeust.com");

        // WHEN
        SerieDto destination = serieMapper.serieToSerieDto(source);

        // THEN
        assertThat(destination).isNotNull();
        assertThat(destination.getId()).isEqualTo(source.getId());
        assertThat(destination.getName()).isEqualTo(source.getName());
        assertThat(destination.getUrl()).isEqualTo(source.getUrl());
    }

    @Test
    @DisplayName("Should return null when Serie source is null")
    void shouldReturnNullForNullSerie() {
        // WHEN
        SerieDto destination = serieMapper.serieToSerieDto(null);

        // THEN
        assertThat(destination).isNull();
    }

    @Test
    @DisplayName("Should map SerieDetails to SerieDetailsDto correctly")
    void shouldMapSerieDetailsToDto() {
        // GIVEN
        SerieDetails source = new SerieDetails(
        "externalId",
        "title",
        "category",
        "status",
        "origin",
        "language",
        "synopsys",
        "originalPictureUrl",
        "pictureUrl",
        "picturePath",
        "pictureFilename",
        1L,
        true,
        "originalPictureThbUrl",
        "pictureThbUrl",
        "pictureThbPath",
        "pictureThbFilename",
        2L,
        true,
        "scrapUrl",
        4,
        "period",
        1984,
        1986,
        "siteUrl",
        "copyright",
        new Serie(),
        new Serie(),
        1,
        new SeriePagination(),
        new SerieRatings(),
        "linkedSeriesPictureUrl",
        List.of( new LinkedSerie()),
        List.of(new ToReadSerie()),
        List.of(new GraphicNovelSideListItem()),
        List.of(new GraphicNovel())
        );

        // WHEN
        SerieDetailsDto destination = serieMapper.serieDetailsToSerieDetailsDto(source);

        // THEN
        assertThat(destination).isNotNull();
        assertThat(destination.getPictureUrl()).isEqualTo(source.getPictureUrl());
        assertThat(destination.getTitle()).isEqualTo(source.getTitle());
        assertThat(destination.getStatus()).isEqualTo(source.getStatus());
        assertThat(destination.getOrigin()).isEqualTo(source.getOrigin());
        assertThat(destination.getLanguage()).isEqualTo(source.getLanguage());
        assertThat(destination.getSynopsys()).isEqualTo(source.getSynopsys());
        assertThat(destination.getLinkedSeries()).hasSize(1);
        assertThat(destination.getToReadSeries()).hasSize(1);
    }

    @Test
    @DisplayName("Should return null when SerieDetails source is null")
    void shouldReturnNullForNullSerieDetails() {
        // WHEN
        SerieDetailsDto destination = serieMapper.serieDetailsToSerieDetailsDto(null);

        // THEN
        assertThat(destination).isNull();
    }

    // === Tests pour le mapping de liste (basés sur la suggestion d'amélioration) ===

    @Test
    @DisplayName("Should map a list of Series to a list of DTOs correctly")
    void shouldMapListOfSeriesToListOfDtos() {
        // GIVEN
        Serie source1 = new Serie();
        Serie source2 = new Serie();
        List<Serie> sourceList = List.of(source1, source2);

        // WHEN
        List<SerieDto> destinationList = serieMapper.seriesToSerieDtos(sourceList);

        // THEN
        assertThat(destinationList).isNotNull().hasSize(2);
    }

    @Test
    @DisplayName("Should return null when list of Series is null")
    void shouldReturnNullForNullSerieList() {
        // WHEN
        List<SerieDto> destinationList = serieMapper.seriesToSerieDtos(null);

        // THEN
        assertThat(destinationList).isNull();
    }

    @Test
    @DisplayName("Should return an empty list when list of Series is empty")
    void shouldReturnEmptyListForEmptySerieList() {
        // WHEN
        List<SerieDto> destinationList = serieMapper.seriesToSerieDtos(Collections.emptyList());

        // THEN
        assertThat(destinationList).isNotNull().isEmpty();
    }
}