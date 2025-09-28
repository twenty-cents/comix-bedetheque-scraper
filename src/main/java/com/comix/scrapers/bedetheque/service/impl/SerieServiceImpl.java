package com.comix.scrapers.bedetheque.service.impl;

import com.comix.scrapers.bedetheque.client.scraper.SerieScraper;
import com.comix.scrapers.bedetheque.rest.mapper.SerieMapper;
import com.comix.scrapers.bedetheque.rest.v1.dto.*;
import com.comix.scrapers.bedetheque.service.OutboxMessageProducer;
import com.comix.scrapers.bedetheque.service.SerieService;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SerieServiceImpl implements SerieService {

    private final SerieScraper serieScraper;

    private final SerieMapper serieMapper = Mappers.getMapper(SerieMapper.class);

    private final OutboxMessageProducer outboxMessageProducer;

    @Value("${amqp.queue.serie.name}")
    private String serieQueueName;

    @Value("${amqp.exchange.serie.name}")
    private String serieExchangeName;

    public SerieServiceImpl(SerieScraper serieScraper, OutboxMessageProducer outboxMessageProducer) {
        this.serieScraper = serieScraper;
        this.outboxMessageProducer = outboxMessageProducer;
    }

    /**
     * Scrap all series indexes
     *
     * @return The list of series indexes
     */
    @Override
    public SeriesByLetterResponseDto scrapSeriesIndexes() {
        List<SeriesByLetterDto> seriesByLetterDtos = serieScraper.listAllSeriesIndexes()
                .stream()
                .map(serieMapper::seriesByLetterToSeriesByLetterDto)
                .toList();
        SeriesByLetterResponseDto seriesByLetterResponseDto = new SeriesByLetterResponseDto();
        seriesByLetterResponseDto.setSeriesByLetter(seriesByLetterDtos);
        return seriesByLetterResponseDto;
    }

    /**
     * Scrap all series indexed by letter
     *
     * @param letter The letter to filter series
     * @return The list of series indexed by letter
     */
    @Override
    public SeriesUrlResponseDto scrapSeriesIndexedByLetter(String letter) {
        List<SerieDto> series = serieScraper.listByLetter(letter)
                .stream()
                .map(serieMapper::serieToSerieDto)
                .toList();
        SeriesUrlResponseDto seriesUrlResponseDto = new SeriesUrlResponseDto();
        seriesUrlResponseDto.setSeriesUrls(series);
        return seriesUrlResponseDto;
    }

    /**
     * Scrap a serie by its url
     *
     * @param url The serie url
     * @return The serie details
     */
    @Override
    public SerieDetailsDto scrap(String url) {
        SerieDetailsDto serieDetailsDto = serieMapper.serieDetailsToSerieDetailsDto(serieScraper.scrap(url));
        outboxMessageProducer.saveToOutbox(serieExchangeName, serieQueueName, serieDetailsDto);
        return serieDetailsDto;
    }
}
