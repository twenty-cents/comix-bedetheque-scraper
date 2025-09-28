package com.comix.scrapers.bedetheque.rest.mapper;

import com.comix.scrapers.bedetheque.client.model.rating.Rating;
import com.comix.scrapers.bedetheque.rest.v1.dto.RatingDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface RatingMapper {

    RatingDto ratingToRatingDto(Rating rating);

    /**
     * Convert a list of Ratings to a list of RatingDtos.
     * MapStruct will automatically implement this method by calling ratingToRatingDto for each element.
     *
     * @param ratings The list of Ratings to convert.
     * @return The list of RatingDtos.
     */
    List<RatingDto> ratingsToRatingDtos(List<Rating> ratings);

}
