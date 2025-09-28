package com.comix.scrapers.bedetheque.service.impl;

import com.comix.scrapers.bedetheque.client.model.author.Author;
import com.comix.scrapers.bedetheque.client.model.author.AuthorsByLetter;
import com.comix.scrapers.bedetheque.client.scraper.AuthorScraper;
import com.comix.scrapers.bedetheque.rest.mapper.AuthorMapper;
import com.comix.scrapers.bedetheque.rest.v1.dto.AuthorUrlDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.AuthorsByLetterDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.AuthorsUrlsResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorServiceImplTest {

    @Mock
    private AuthorScraper authorScraper;

    @Mock
    private AuthorMapper authorMapper;

    @InjectMocks
    private AuthorServiceImpl authorService;

    @Test
    void scrapAuthorsIndexes_shouldReturnAuthorsByLetter() {
        // Given
        List<AuthorsByLetter> authorsByLetters = new ArrayList<>();
        authorsByLetters.add(new AuthorsByLetter("A", "https://url.com/A"));
        authorsByLetters.add(new AuthorsByLetter("B", "https://url.com/B"));

        when(authorScraper.listAllAuthorsIndexes()).thenReturn(authorsByLetters);
        when(authorMapper.authorsByLetterToAuthorsByLetterDto(any(AuthorsByLetter.class))).thenAnswer(i -> {
            AuthorsByLetter arg = i.getArgument(0);
            return new AuthorsByLetterDto().letter(arg.getLetter()).url(arg.getUrl());
        });

        // When
        var result = authorService.scrapAuthorsIndexes();

        // Then
        assertNotNull(result);
        assertEquals(2, result.getAuthorsByLetter().size());
        assertEquals("A", result.getAuthorsByLetter().getFirst().getLetter());
        assertEquals("https://url.com/A", result.getAuthorsByLetter().getFirst().getUrl());
    }

    @Test
    void scrapAuthorsIndexedByLetter_shouldReturnAuthorsUrls() {
        // Given
        String letter = "A";
        List<Author> authors = new ArrayList<>();
        authors.add(new Author("1", "Author 1", "https://url.com/1"));
        authors.add(new Author("2", "Author 2", "https://url.com/2"));

        when(authorScraper.scrapAuthorsIndexedByLetter(letter)).thenReturn(authors);
        when(authorMapper.authorToAuthorDto(any(Author.class))).thenAnswer(i -> {
            Author arg = i.getArgument(0);
            return new AuthorUrlDto().id(arg.getId()).name(arg.getName()).url(arg.getUrl());
        });

        // When
        AuthorsUrlsResponseDto result = authorService.scrapAuthorsIndexedByLetter(letter);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getAuthorsUrls().size());
        assertEquals("1", result.getAuthorsUrls().getFirst().getId());
        assertEquals("Author 1", result.getAuthorsUrls().getFirst().getName());
        assertEquals("https://url.com/1", result.getAuthorsUrls().getFirst().getUrl());
    }

//    @Test
//    void scrap_shouldReturnAuthorDetails() {
//        // Given
//        String url = "https://url.com/author/1";
//        AuthorDetails authorDetails = new AuthorDetails();
//        authorDetails.setId("1");
//        authorDetails.setLastname("Lastname");
//        authorDetails.setFirstname("Firstname");
//        authorDetails.setNickname("Nickname");
//        authorDetails.setNationality("Nationality");
//        authorDetails.setBirthdate("Birthdate");
//        authorDetails.setDeceaseDate("Decease Date");
//        authorDetails.setBiography("Biography");
//        authorDetails.setSiteUrl("Site URL");
//        authorDetails.setOtherAuthorPseudonym(null);
//        authorDetails.setPhotoUrl("Photo URL");
//        authorDetails.setPhotoThbUrl("Photo Thb URL");
//        authorDetails.setAuthorUrl("Author URL");
//        authorDetails.setPreviousAuthor(null);
//        authorDetails.setNextAuthor(null);
//        authorDetails.setAuthorsToDiscover(Collections.emptyList());
//        authorDetails.setBibliography(Collections.emptyList());
//        authorDetails.setSeriesToDiscover(Collections.emptyList());
//
//        AuthorDetailsDto authorDetailsDto = new AuthorDetailsDto()
//                .id("1")
//                .lastname("Lastname")
//                .firstname("Firstname")
//                .nickname("Nickname")
//                .nationality("Nationality")
//                .birthdate("Birthdate")
//                .deceaseDate("Decease Date")
//                .biography("Biography")
//                .siteUrl("Site URL")
//                .otherAuthorPseudonym(null)
//                .photoUrl("Photo URL")
//                .photoThbUrl("Photo Thb URL")
//                .authorUrl("Author URL")
//                .previousAuthor(null)
//                .nextAuthor(null)
//                .authorsToDiscover(Collections.emptyList())
//                .bibliography(Collections.emptyList())
//                .seriesToDiscover(Collections.emptyList());
//
//        when(authorScraper.scrap(url)).thenReturn(authorDetails);
//        when(authorMapper.authorDetailsToAuthorDetailsDto(authorDetails)).thenReturn(authorDetailsDto);
//
//        // When
//        AuthorDetailsDto result = authorService.scrap(url);
//
//        // Then
//        assertNotNull(result);
//        assertEquals("1", result.getId());
//        assertEquals("Lastname", result.getLastname());
//        assertEquals("Firstname", result.getFirstname());
//    }
}
