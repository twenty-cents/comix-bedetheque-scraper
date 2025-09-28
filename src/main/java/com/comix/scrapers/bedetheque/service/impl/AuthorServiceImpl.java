package com.comix.scrapers.bedetheque.service.impl;

import com.comix.scrapers.bedetheque.client.scraper.AuthorScraper;
import com.comix.scrapers.bedetheque.rest.mapper.AuthorMapper;
import com.comix.scrapers.bedetheque.rest.v1.dto.*;
import com.comix.scrapers.bedetheque.service.AuthorService;
import com.comix.scrapers.bedetheque.service.OutboxMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorScraper authorScraper;
    private final AuthorMapper authorMapper;
    private final OutboxMessageProducer outboxMessageProducer;

    @Value("${amqp.queue.author.name}")
    private String authorQueueName;

    @Value("${amqp.exchange.author.name}")
    private String authorExchangeName;

    public AuthorServiceImpl(AuthorScraper authorScraper,
                             AuthorMapper authorMapper,
                             OutboxMessageProducer outboxMessageProducer) {
        this.authorScraper = authorScraper;
        this.authorMapper = authorMapper;
        this.outboxMessageProducer = outboxMessageProducer;
    }

    /**
     * Scrap alphabetical indexes urls of all authors
     *
     * @return the list of alphabetical indexes urls of all authors
     */
    @Override
    public AuthorsByLetterResponseDto scrapAuthorsIndexes() {
        AuthorsByLetterResponseDto authorsResponseDto = new AuthorsByLetterResponseDto();
        List<AuthorsByLetterDto> authorsByLetterDtos = authorScraper.listAllAuthorsIndexes()
                .stream()
                .map(authorMapper::authorsByLetterToAuthorsByLetterDto)
                .toList();
        authorsResponseDto.setAuthorsByLetter(authorsByLetterDtos);
        return authorsResponseDto;
    }

    /**
     * Scrap the list of all authors starting with a given letter
     *
     * @param letter The letter to filter authors urls
     * @return the list of all authors starting with a given letter
     */
    @Override
    public AuthorsUrlsResponseDto scrapAuthorsIndexedByLetter(String letter) {
        AuthorsUrlsResponseDto authorsUrlsResponseDto = new AuthorsUrlsResponseDto();
        List<AuthorUrlDto> authorUrlDtos = authorScraper.scrapAuthorsIndexedByLetter(letter)
                .stream()
                .map(authorMapper::authorToAuthorDto)
                .toList();
        authorsUrlsResponseDto.setAuthorsUrls(authorUrlDtos);
        return authorsUrlsResponseDto;
    }

    /**
     * Scrap author data
     *
     * @param url The author url at <a href="https://www.bedetheque.com">...</a>
     * @return the scraped author data
     */
    @Override
    public AuthorDetailsDto scrap(String url) {
        AuthorDetailsDto authorDetailsDto = authorMapper.authorDetailsToAuthorDetailsDto(authorScraper.scrap(url));
        outboxMessageProducer.saveToOutbox(authorExchangeName, authorQueueName, authorDetailsDto);
        return authorDetailsDto;
    }
}
