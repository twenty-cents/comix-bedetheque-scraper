package com.comix.scrapers.bedetheque.rest.mapper.mapper;

import com.comix.scrapers.bedetheque.client.model.rating.Rating;
import com.comix.scrapers.bedetheque.rest.mapper.RatingMapper;
import com.comix.scrapers.bedetheque.rest.v1.dto.RatingDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RatingMapperTest {

    // On récupère une instance de l'implémentation du mapper générée par MapStruct
    private final RatingMapper ratingMapper = Mappers.getMapper(RatingMapper.class);

    @Test
    @DisplayName("Should map Rating to RatingDto correctly")
    void shouldMapRatingToDto() {
        // GIVEN: un objet source avec des données de test
        Rating source = new Rating(
                "graphicNovelTitle",
                "graphicNovelUrl",
                "graphicNovelPictureUrl",
                "graphicNovelPictureTitle",
                "createBy",
                "createByAllRatingsUrl",
                "createOn",
                "ratingPictureUrl",
                "ratingTitle",
                "comment"
        );

        // WHEN: on appelle la méthode de mapping
        RatingDto destination = ratingMapper.ratingToRatingDto(source);

        // THEN: on vérifie que l'objet de destination est correct
        assertThat(destination).isNotNull();
        assertThat(destination.getGraphicNovelTitle()).isEqualTo(source.getGraphicNovelTitle());
        assertThat(destination.getGraphicNovelUrl()).isEqualTo(source.getGraphicNovelUrl());
        assertThat(destination.getRatingPictureUrl()).isEqualTo(source.getRatingPictureUrl());
        assertThat(destination.getGraphicNovelPictureTitle()).isEqualTo(source.getGraphicNovelPictureTitle());
        assertThat(destination.getCreateBy()).isEqualTo(source.getCreateBy());
        assertThat(destination.getCreateByAllRatingsUrl()).isEqualTo(source.getCreateByAllRatingsUrl());
        assertThat(destination.getCreateOn()).isEqualTo(source.getCreateOn());
        assertThat(destination.getRatingPictureUrl()).isEqualTo(source.getRatingPictureUrl());
        assertThat(destination.getRatingTitle()).isEqualTo(source.getRatingTitle());
        assertThat(destination.getComment()).isEqualTo(source.getComment());
    }

    @Test
    @DisplayName("Should return null when Rating source is null")
    void shouldReturnNullForNullRating() {
        // WHEN
        RatingDto destination = ratingMapper.ratingToRatingDto(null);

        // THEN
        assertThat(destination).isNull();
    }

    // === Tests pour le mapping de liste (basés sur la suggestion d'amélioration) ===

    @Test
    @DisplayName("Should map a list of Ratings to a list of DTOs correctly")
    void shouldMapListOfRatingsToListOfDtos() {
        // GIVEN
        Rating source1 = new Rating();
        Rating source2 = new Rating();
        List<Rating> sourceList = List.of(source1, source2);

        // WHEN
        List<RatingDto> destinationList = ratingMapper.ratingsToRatingDtos(sourceList);

        // THEN
        assertThat(destinationList).isNotNull().hasSize(2);
    }

    @Test
    @DisplayName("Should return null when list of Ratings is null")
    void shouldReturnNullForNullList() {
        // WHEN
        List<RatingDto> destinationList = ratingMapper.ratingsToRatingDtos(null);

        // THEN
        assertThat(destinationList).isNull();
    }

    @Test
    @DisplayName("Should return an empty list when list of Ratings is empty")
    void shouldReturnEmptyListForEmptyList() {
        // WHEN
        List<RatingDto> destinationList = ratingMapper.ratingsToRatingDtos(Collections.emptyList());

        // THEN
        assertThat(destinationList).isNotNull().isEmpty();
    }
}