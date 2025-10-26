package com.comix.scrapers.bedetheque.rest.mapper.mapper;

import com.comix.scrapers.bedetheque.client.model.graphicnovel.AuthorRole;
import com.comix.scrapers.bedetheque.client.model.graphicnovel.GraphicNovel;
import com.comix.scrapers.bedetheque.client.model.graphicnovel.Ratings;
import com.comix.scrapers.bedetheque.client.model.serie.Serie;
import com.comix.scrapers.bedetheque.rest.mapper.GraphicNovelMapper;
import com.comix.scrapers.bedetheque.rest.v1.dto.GraphicNovelDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GraphicNovelMapperTest {

    // On récupère une instance de l'implémentation du mapper générée par MapStruct
    private final GraphicNovelMapper graphicNovelMapper = Mappers.getMapper(GraphicNovelMapper.class);

    @Test
    @DisplayName("Should map GraphicNovel to GraphicNovelDto correctly")
    void shouldMapGraphicNovelToDto() {
        // GIVEN: un objet source avec des données de test
        GraphicNovel source = new GraphicNovel(
        "externalId",
        "tome",
        1,
        "numEdition",
        "title",
                List.of(new AuthorRole()),
        "publicationDate",
        "releaseDate",
        "publisher",
        "collection",
        "collectionUrl",
        "isbn",
        45,
        "format",
        true,
        false,
        false,
        "infoEdition",
        "reeditionUrl",
        "reeditionCount",
        "externalIdOriginalPublication",
        "coverPictureUrl",
        "coverThumbnailUrl",
        "backCoverPictureUrl",
        "backCoverThumbnailUrl",
        "pagePictureUrl",
        "pageThumbnailUrl",
        "copyright",
        "scrapUrl",
        new Ratings(),
                new Serie()
        );

        // WHEN: on appelle la méthode de mapping
        GraphicNovelDto destination = graphicNovelMapper.graphicNovelToGraphicNovelDto(source);

        // THEN: on vérifie que l'objet de destination est correct
        assertThat(destination).isNotNull();
        assertThat(destination.getExternalId()).isEqualTo(source.getExternalId());
        assertThat(destination.getTitle()).isEqualTo(source.getTitle());
        assertThat(destination.getTome()).isEqualTo(source.getTome());
        assertThat(destination.getPublisher()).isEqualTo(source.getPublisher());
        assertThat(destination.getPublicationDate()).isEqualTo(source.getPublicationDate());
        assertThat(destination.getIsbn()).isEqualTo(source.getIsbn());
        assertThat(destination.getCoverPictureUrl()).isEqualTo(source.getCoverPictureUrl());
        assertThat(destination.getCoverThumbnailUrl()).isEqualTo(source.getCoverThumbnailUrl());
        assertThat(destination.getBackCoverPictureUrl()).isEqualTo(source.getBackCoverPictureUrl());
        assertThat(destination.getBackCoverThumbnailUrl()).isEqualTo(source.getBackCoverThumbnailUrl());
        assertThat(destination.getPagePictureUrl()).isEqualTo(source.getPagePictureUrl());
        assertThat(destination.getPageThumbnailUrl()).isEqualTo(source.getPageThumbnailUrl());
        assertThat(destination.getTitle()).isEqualTo(source.getTitle());
        assertThat(destination.getScrapUrl()).isEqualTo(source.getScrapUrl());
        assertThat(destination.getRatings()).isNotNull();
    }

    @Test
    @DisplayName("Should return null when GraphicNovel source is null")
    void shouldReturnNullForNullGraphicNovel() {
        // WHEN
        GraphicNovelDto destination = graphicNovelMapper.graphicNovelToGraphicNovelDto(null);

        // THEN
        assertThat(destination).isNull();
    }

    @Test
    @DisplayName("Should map a list of GraphicNovels to a list of DTOs correctly")
    void shouldMapListOfGraphicNovelsToListOfDtos() {
        // GIVEN
        GraphicNovel source1 = new GraphicNovel();
        GraphicNovel source2 = new GraphicNovel();
        List<GraphicNovel> sourceList = List.of(source1, source2);

        // WHEN
        List<GraphicNovelDto> destinationList = graphicNovelMapper.graphicNovelToGraphicNovelsDto(sourceList);

        // THEN
        assertThat(destinationList).isNotNull().hasSize(2);
    }

    @Test
    @DisplayName("Should return null when list of GraphicNovels is null")
    void shouldReturnNullForNullList() {
        // WHEN
        List<GraphicNovelDto> destinationList = graphicNovelMapper.graphicNovelToGraphicNovelsDto(null);

        // THEN
        assertThat(destinationList).isNull();
    }

    @Test
    @DisplayName("Should return an empty list when list of GraphicNovels is empty")
    void shouldReturnEmptyListForEmptyList() {
        // WHEN
        List<GraphicNovelDto> destinationList = graphicNovelMapper.graphicNovelToGraphicNovelsDto(Collections.emptyList());

        // THEN
        assertThat(destinationList).isNotNull().isEmpty();
    }
}