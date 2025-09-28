package com.comix.scrapers.bedetheque.rest.controller;

import com.comix.scrapers.bedetheque.exception.BusinessException;
import com.comix.scrapers.bedetheque.rest.v1.dto.*;
import com.comix.scrapers.bedetheque.service.AuthorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.comix.scrapers.bedetheque.rest.controller.AuthorController.SCRAP_AUTHORS_URLS_BY_LETTER;
import static com.comix.scrapers.bedetheque.rest.controller.AuthorController.SCRAP_AUTHORS_URLS_INDEXES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorControllerTest {

    @Mock
    private AuthorService authorService;

    @InjectMocks
    private AuthorController authorController;

    //=========================================================================
    // Tests for scrapAuthors
    //=========================================================================

    @Test
    @DisplayName("scrapAuthors should call service to get indexes when action is SCRAP_AUTHORS_URLS_INDEXES")
    void scrapAuthors_withIndexesAction_shouldCallServiceAndReturnOk() {
        // GIVEN
        AuthorsByLetterResponseDto mockResponse = new AuthorsByLetterResponseDto();
        mockResponse.setAuthorsByLetter(List.of(new AuthorsByLetterDto("A", "https://a.com")));
        when(authorService.scrapAuthorsIndexes()).thenReturn(mockResponse);

        // WHEN
        ResponseEntity<AuthorsResponseDto> response = authorController.scrapAuthors(SCRAP_AUTHORS_URLS_INDEXES, null);

        // THEN
        verify(authorService, times(1)).scrapAuthorsIndexes();
        verify(authorService, never()).scrapAuthorsIndexedByLetter(anyString());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(mockResponse);
    }

    @Test
    @DisplayName("scrapAuthors should call service to get authors by letter when action is SCRAP_AUTHORS_URLS_BY_LETTER")
    void scrapAuthors_withByLetterAction_shouldCallServiceAndReturnOk() {
        // GIVEN
        String letter = "B";
        AuthorsUrlsResponseDto mockResponse = new AuthorsUrlsResponseDto();
        mockResponse.setAuthorsUrls(List.of(new AuthorUrlDto("id", "Author B", "https://b.com")));
        when(authorService.scrapAuthorsIndexedByLetter(letter)).thenReturn(mockResponse);

        // WHEN
        ResponseEntity<AuthorsResponseDto> response = authorController.scrapAuthors(SCRAP_AUTHORS_URLS_BY_LETTER, letter);

        // THEN
        verify(authorService, never()).scrapAuthorsIndexes();
        verify(authorService, times(1)).scrapAuthorsIndexedByLetter(letter);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(mockResponse);
    }

    @Test
    @DisplayName("scrapAuthors should throw BusinessException for unsupported action")
    void scrapAuthors_withUnsupportedAction_shouldThrowBusinessException() {
        // GIVEN
        String unsupportedAction = "INVALID_ACTION";

        // WHEN & THEN
        assertThatThrownBy(() -> authorController.scrapAuthors(unsupportedAction, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("codeMessage", "UNSUPPORTED_ACTION");

        verify(authorService, never()).scrapAuthorsIndexes();
        verify(authorService, never()).scrapAuthorsIndexedByLetter(anyString());
    }

    @Test
    @DisplayName("scrapAuthors should throw BusinessException when letter is missing for SCRAP_AUTHORS_URLS_BY_LETTER action")
    void scrapAuthors_withByLetterActionAndMissingLetter_shouldThrowBusinessException() {
        // GIVEN
        // WHEN & THEN
        assertThatThrownBy(() -> authorController.scrapAuthors(SCRAP_AUTHORS_URLS_BY_LETTER, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("codeMessage", "LETTER_NOT_FOUND");

        verify(authorService, never()).scrapAuthorsIndexes();
        verify(authorService, never()).scrapAuthorsIndexedByLetter(anyString());
    }

    //=========================================================================
    // Tests for scrapAuthor
    //=========================================================================

    @Test
    @DisplayName("scrapAuthor should call service with URL and return author details")
    void scrapAuthor_withValidUrl_shouldCallServiceAndReturnOk() {
        // GIVEN
        String authorId = "123";
        String url = "https://author.com/123";
        AuthorDetailsDto mockDetails = new AuthorDetailsDto();
        mockDetails.setFirstname("John");
        mockDetails.setLastname("Doe");
        when(authorService.scrap(url)).thenReturn(mockDetails);

        // WHEN
        ResponseEntity<AuthorDetailsDto> response = authorController.scrapAuthor(authorId, url);

        // THEN
        verify(authorService, times(1)).scrap(url);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFirstname()).isEqualTo("John");
    }

    @Test
    @DisplayName("scrapAuthor should return OK with a null body when service finds nothing")
    void scrapAuthor_whenServiceReturnsNull_shouldReturnOkWithNullBody() {
        // GIVEN
        String authorId = "404";
        String url = "https://author.com/404";
        when(authorService.scrap(url)).thenReturn(null);

        // WHEN
        ResponseEntity<AuthorDetailsDto> response = authorController.scrapAuthor(authorId, url);

        // THEN
        verify(authorService, times(1)).scrap(url);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
    }
}