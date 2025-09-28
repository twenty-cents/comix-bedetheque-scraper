package com.comix.scrapers.bedetheque.service;

import com.comix.scrapers.bedetheque.client.model.filter.GraphicNovelsFilters;
import com.comix.scrapers.bedetheque.rest.v1.dto.AutocompleteSearchDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.GlobalFilteredDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.GraphicNovelsFilteredDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FilterService {

    /**
     * Autocomplete search for authors
     *
     * @param filter The filter to apply
     * @return The list of autocomplete search results
     */
    List<AutocompleteSearchDto> autocompleteAuthors(String filter);

    /**
     * Autocomplete search for categories
     *
     * @param filter The filter to apply
     * @return The list of autocomplete search results
     */
    List<AutocompleteSearchDto> autocompleteCategories(String filter);

    /**
     * Autocomplete search for collections
     *
     * @param filter The filter to apply
     * @return The list of autocomplete search results
     */
    List<AutocompleteSearchDto> autocompleteCollections(String filter);

    /**
     * Autocomplete search for publishers
     *
     * @param filter The filter to apply
     * @return The list of autocomplete search results
     */
    List<AutocompleteSearchDto> autocompletePublishers(String filter);

    /**
     * Autocomplete search for series
     *
     * @param filter The filter to apply
     * @return The list of autocomplete search results
     */
    List<AutocompleteSearchDto> autocompleteSeries(String filter);

    /**
     * Global search (series, authors, graphic novels, news...)
     *
     * @param filter the information to search.
     * @return The list of theses objets scraped from
     */
    GlobalFilteredDto globalSearch(String filter);

    /**
     * Graphic novels search
     *
     * @param graphicNovelsFilters The filters to apply
     * @return The list of graphic novels scraped from the url
     */
    GraphicNovelsFilteredDto graphicNovelSearch(GraphicNovelsFilters graphicNovelsFilters);
}
