package com.comix.scrapers.bedetheque.service.impl;

import com.comix.scrapers.bedetheque.client.model.filter.GraphicNovelsFilters;
import com.comix.scrapers.bedetheque.client.scraper.FilterGlobalScraper;
import com.comix.scrapers.bedetheque.client.scraper.FilterGraphicNovelsScraper;
import com.comix.scrapers.bedetheque.rest.mapper.FilterMapper;
import com.comix.scrapers.bedetheque.rest.v1.dto.AutocompleteSearchDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.GlobalFilteredDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.GraphicNovelsFilteredDto;
import com.comix.scrapers.bedetheque.service.FilterService;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FilterServiceImpl implements FilterService {

    private final FilterGlobalScraper filterGlobalScraper;
    private final FilterGraphicNovelsScraper filterGraphicNovelsScraper;

    private final FilterMapper filterMapper = Mappers.getMapper(FilterMapper.class);

    public FilterServiceImpl(FilterGlobalScraper filterGlobalScraper, FilterGraphicNovelsScraper filterGraphicNovelsScraper) {
        this.filterGlobalScraper = filterGlobalScraper;
        this.filterGraphicNovelsScraper = filterGraphicNovelsScraper;
    }

    /**
     * Autocomplete search for authors
     *
     * @param filter The filter to apply
     * @return The list of autocomplete search results
     */
    @Override
    public List<AutocompleteSearchDto> autocompleteAuthors(String filter) {
        return filterGraphicNovelsScraper.autocompleteAuthors(filter)
                .stream()
                .map(filterMapper::autocompleteSearchToAutocompleteSearchDto)
                .toList();
    }

    /**
     * Autocomplete search for categories
     *
     * @param filter The filter to apply
     * @return The list of autocomplete search results
     */
    @Override
    public List<AutocompleteSearchDto> autocompleteCategories(String filter) {
        return filterGraphicNovelsScraper.autocompleteCategories(filter)
                .stream()
                .map(filterMapper::autocompleteSearchToAutocompleteSearchDto)
                .toList();
    }

    /**
     * Autocomplete search for collections
     *
     * @param filter The filter to apply
     * @return The list of autocomplete search results
     */
    @Override
    public List<AutocompleteSearchDto> autocompleteCollections(String filter) {
        return filterGraphicNovelsScraper.autocompleteCollections(filter)
                .stream()
                .map(filterMapper::autocompleteSearchToAutocompleteSearchDto)
                .toList();
    }

    /**
     * Autocomplete search for publishers
     *
     * @param filter The filter to apply
     * @return The list of autocomplete search results
     */
    @Override
    public List<AutocompleteSearchDto> autocompletePublishers(String filter) {
        return filterGraphicNovelsScraper.autocompletePublishers(filter)
                .stream()
                .map(filterMapper::autocompleteSearchToAutocompleteSearchDto)
                .toList();
    }

    /**
     * Autocomplete search for series
     *
     * @param filter The filter to apply
     * @return The list of autocomplete search results
     */
    @Override
    public List<AutocompleteSearchDto> autocompleteSeries(String filter) {
        return filterGraphicNovelsScraper.autocompleteSeries(filter)
                .stream()
                .map(filterMapper::autocompleteSearchToAutocompleteSearchDto)
                .toList();
    }

    /**
     * Global search (series, authors, graphic novels, news...)
     *
     * @param filter the information to search.
     * @return The list of theses objets scraped from
     */
    @Override
    public GlobalFilteredDto globalSearch(String filter) {
        return filterMapper.globalFilteredObjectToGlobalFilteredDto(filterGlobalScraper.filter(filter));
    }

    /**
     * Graphic novels search
     *
     * @param graphicNovelsFilters The filters to apply
     * @return The list of graphic novels scraped from the url
     */
    @Override
    public GraphicNovelsFilteredDto graphicNovelSearch(GraphicNovelsFilters graphicNovelsFilters) {
        return filterMapper.graphicNovelsFilteredObjectToGraphicNovelsFilteredDto(filterGraphicNovelsScraper.filter(graphicNovelsFilters));
    }
}

