package com.comix.scrapers.bedetheque.rest.mapper;

import com.comix.scrapers.bedetheque.client.model.serie.Serie;
import com.comix.scrapers.bedetheque.client.model.serie.SerieDetails;
import com.comix.scrapers.bedetheque.client.model.serie.SeriesByLetter;
import com.comix.scrapers.bedetheque.rest.v1.dto.SerieDetailsDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.SerieDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.SeriesByLetterDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface SerieMapper {

    /**
     * Convert a SeriesByLetter to a SeriesByLetterDto
     *
     * @param seriesByLetter The SeriesByLetter to convert
     * @return The SeriesByLetterDto
     */
    SeriesByLetterDto seriesByLetterToSeriesByLetterDto(SeriesByLetter seriesByLetter);

    /**
     * Convert a Serie to a SerieDto
     *
     * @param serie The Serie to convert
     * @return The SerieDto
     */
    SerieDto serieToSerieDto(Serie serie);

    /**
     * Convert a SerieDetails to a SerieDetailsDto
     *
     * @param serieDetails The SerieDetails to convert
     * @return The SerieDetailsDto
     */
    SerieDetailsDto serieDetailsToSerieDetailsDto(SerieDetails serieDetails);

    /**
     * Convert a list of Series to a list of SerieDtos.
     * @param series The list of Series to convert.
     * @return The list of SerieDtos.
     */
    List<SerieDto> seriesToSerieDtos(List<Serie> series);

    /**
     * Convert a list of SeriesByLetter to a list of SeriesByLetterDtos.
     * @param seriesByLetters The list of SeriesByLetter to convert.
     * @return The list of SeriesByLetterDtos.
     */
    List<SeriesByLetterDto> seriesByLettersToSeriesByLetterDtos(List<SeriesByLetter> seriesByLetters);
}
