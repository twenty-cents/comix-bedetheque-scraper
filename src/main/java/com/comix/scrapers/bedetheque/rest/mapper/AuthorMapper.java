package com.comix.scrapers.bedetheque.rest.mapper;

import com.comix.scrapers.bedetheque.client.model.author.Author;
import com.comix.scrapers.bedetheque.client.model.author.AuthorDetails;
import com.comix.scrapers.bedetheque.client.model.author.AuthorsByLetter;
import com.comix.scrapers.bedetheque.rest.v1.dto.AuthorDetailsDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.AuthorUrlDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.AuthorsByLetterDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthorMapper {

    /**
     * Convert an AuthorsByLetter to an AuthorsByLetterDto
     * @param authorsByLetter the AuthorsByLetter to convert
     * @return the AuthorsByLetterDto
     */
    AuthorsByLetterDto authorsByLetterToAuthorsByLetterDto(AuthorsByLetter authorsByLetter);

    /**
     * Convert an Author to an AuthorDto
     * @param author the Author to convert
     * @return the AuthorDto
     */
    AuthorUrlDto authorToAuthorDto(Author author);

    /**
     * Convert an AuthorDetails to an AuthorDetailsDto
     * @param authorDetails the AuthorDetails to convert
     * @return the AuthorDetailsDto
     */
    AuthorDetailsDto authorDetailsToAuthorDetailsDto(AuthorDetails authorDetails);
}
