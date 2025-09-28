package com.comix.scrapers.bedetheque.service.impl;

import com.comix.scrapers.bedetheque.client.model.graphicnovel.GraphicNovel;
import com.comix.scrapers.bedetheque.client.model.graphicnovel.GraphicNovelPage;
import com.comix.scrapers.bedetheque.client.scraper.GraphicNovelScraper;
import com.comix.scrapers.bedetheque.rest.mapper.GraphicNovelMapper;
import com.comix.scrapers.bedetheque.rest.v1.dto.GraphicNovelDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.ScrapAllRepublicationsResponseDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.ScrapGraphicNovelsResponseDto;
import com.comix.scrapers.bedetheque.service.GraphicNovelService;
import com.comix.scrapers.bedetheque.service.OutboxMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class GraphicNovelServiceImpl implements GraphicNovelService {

    private final GraphicNovelScraper graphicNovelScraper;

    private final GraphicNovelMapper graphicNovelMapper = Mappers.getMapper(GraphicNovelMapper.class);

    private final OutboxMessageProducer outboxMessageProducer;

    @Value("${amqp.queue.comic.name}")
    private String comicQueueName;

    @Value("${amqp.exchange.comic.name}")
    private String comicExchangeName;

    public GraphicNovelServiceImpl(GraphicNovelScraper graphicNovelScraper,
                                   OutboxMessageProducer outboxMessageProducer) {
        this.graphicNovelScraper = graphicNovelScraper;
        this.outboxMessageProducer = outboxMessageProducer;
    }

    /**
     * Scrap all graphics novels
     *
     * @param serieUrl The graphic novels url
     * @param page     The page number (optional)
     * @return The list of graphic novels scraped from the url
     */
    @Override
    public ScrapGraphicNovelsResponseDto scrap(String serieUrl, Integer page) {
        if(page == null) {
            page = 10000;
        }
        GraphicNovelPage graphicNovelPage = graphicNovelScraper.scrapElement(serieUrl, page);
        ScrapGraphicNovelsResponseDto scrapGraphicNovelsResponseDto = new ScrapGraphicNovelsResponseDto();
        scrapGraphicNovelsResponseDto.setGraphicNovels(graphicNovelMapper.graphicNovelToGraphicNovelsDto(graphicNovelPage.getGraphicNovels()));
        scrapGraphicNovelsResponseDto.setPage(graphicNovelPage.getPage());
        scrapGraphicNovelsResponseDto.setSize(graphicNovelPage.getSize());
        scrapGraphicNovelsResponseDto.setTotalElements(graphicNovelPage.getTotalElements());
        scrapGraphicNovelsResponseDto.setTotalPages(graphicNovelPage.getTotalPages());

        for (GraphicNovelDto graphicNovelDto : scrapGraphicNovelsResponseDto.getGraphicNovels()) {
            outboxMessageProducer.saveToOutbox(comicExchangeName, comicQueueName, graphicNovelDto);
        }

        return scrapGraphicNovelsResponseDto;
    }

    /**
     * Scrap all republications of a graphic novel
     *
     * @param graphicNovelUrl The url of the graphic novel
     * @return The list of graphic novels scraped from the url
     */
    @Override
    public ScrapAllRepublicationsResponseDto scrapWithAllRepublications(String graphicNovelUrl) {
        List<GraphicNovel> graphicNovels = graphicNovelScraper.scrapWithAllRepublications(graphicNovelUrl);
        ScrapAllRepublicationsResponseDto scrapAllRepublicationsResponseDto = new ScrapAllRepublicationsResponseDto();
        scrapAllRepublicationsResponseDto.setGraphicNovels(graphicNovelMapper.graphicNovelToGraphicNovelsDto(graphicNovels));
        return scrapAllRepublicationsResponseDto;
    }
}
