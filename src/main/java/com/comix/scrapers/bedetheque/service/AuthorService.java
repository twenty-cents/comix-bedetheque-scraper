package com.comix.scrapers.bedetheque.service;

import com.comix.scrapers.bedetheque.rest.v1.dto.AuthorDetailsDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.AuthorsByLetterResponseDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.AuthorsUrlsResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface AuthorService {

    /**
     * Scrap alphabetical indexes urls of all authors
     *
     * @return the list of alphabetical indexes urls of all authors
     */
    AuthorsByLetterResponseDto scrapAuthorsIndexes();

    /**
     * Scrap the list of all authors starting with a given letter
     *
     * @param letter The letter to filter authors urls
     * @return the list of all authors starting with a given letter
     */
    AuthorsUrlsResponseDto scrapAuthorsIndexedByLetter(String letter);

    /**
     * Scrap author data
     *
     * @param url The author url at <a href="https://www.bedetheque.com">...</a>
     * @return the scraped author data
     */
    AuthorDetailsDto scrap(String url);
}
