package com.comix.scrapers.bedetheque.rest.controller;

import com.comix.scrapers.bedetheque.exception.BusinessException;
import com.comix.scrapers.bedetheque.rest.v1.api.AuthorsApi;
import com.comix.scrapers.bedetheque.rest.v1.dto.AuthorDetailsDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.AuthorsResponseDto;
import com.comix.scrapers.bedetheque.service.AuthorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthorController implements AuthorsApi, V1Controller {

    public static final String SCRAP_AUTHORS_URLS_INDEXES = "SCRAP_AUTHORS_URLS_INDEXES";
    public static final String SCRAP_AUTHORS_URLS_BY_LETTER = "SCRAP_AUTHORS_URLS_BY_LETTER";


    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorController.class);

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    /**
     * Scrap authors data
     *
     * @param action The action to execute.
     *               - SCRAP_AUTHORS_URLS_INDEXES : Scrap alphabetical indexes urls of all authors.
     *               - SCRAP_AUTHORS_URLS_BY_LETTER : Scrap the list of all authors starting with a given letter. (required)
     * @param letter The letter to filter authors' urls.  Mandatory if the action is SCRAP_AUTHORS_URLS_BY_LETTER. (optional)
     * @return scraped authors data
     */
    @Override
    public ResponseEntity<AuthorsResponseDto> scrapAuthors(String action, String letter) {
        // Check if the action is available
        if (!(action.equals(SCRAP_AUTHORS_URLS_BY_LETTER) || action.equals(SCRAP_AUTHORS_URLS_INDEXES))) {
            throw new BusinessException("UNSUPPORTED_ACTION", new Object[]{action});
        }

        // Check if a letter is provided if the action is scrap the list of all authors starting with a given letter
        if (action.equals(SCRAP_AUTHORS_URLS_BY_LETTER) && letter == null) {
            throw new BusinessException("LETTER_NOT_FOUND", new Object[]{action});
        }

        return switch (action) {
            case SCRAP_AUTHORS_URLS_INDEXES -> {
                LOGGER.info("Bedetheque - Scrap all authors indexes");
                yield ResponseEntity
                        .status(HttpStatus.OK)
                        .body(authorService.scrapAuthorsIndexes());
            }
            case SCRAP_AUTHORS_URLS_BY_LETTER -> {
                LOGGER.info("Bedetheque - Scrap all authors starting with letter {}", letter);
                yield ResponseEntity
                        .status(HttpStatus.OK)
                        .body(authorService.scrapAuthorsIndexedByLetter(letter));
            }
            default -> throw new BusinessException("UNSUPPORTED_ACTION", new Object[]{action});
        };

    }

    /**
     * Scrap author data
     *
     * @param authorId The author id at bedetheque.com (required)
     * @param url      The author url at <a href="https://www.bedetheque.com">...</a>. (required)
     * @return the scraped author data
     */
    @Override
    public ResponseEntity<AuthorDetailsDto> scrapAuthor(String authorId, String url) {
        LOGGER.info("Bedetheque - Scrap author data (id {}) from url {}", authorId, url);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authorService.scrap(url));
    }
}
