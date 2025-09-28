package com.comix.scrapers.bedetheque.rest.controller;

import com.comix.scrapers.bedetheque.client.model.filter.GraphicNovelsFilters;
import com.comix.scrapers.bedetheque.client.model.filter.SerieOrigin;
import com.comix.scrapers.bedetheque.client.model.filter.SerieStatus;
import com.comix.scrapers.bedetheque.client.model.serie.SerieLanguage;
import com.comix.scrapers.bedetheque.rest.v1.api.FiltersApi;
import com.comix.scrapers.bedetheque.rest.v1.dto.AutocompleteSearchDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.FilterAutocompleteTypeEnumDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.GlobalFilteredDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.GraphicNovelsFilteredDto;
import com.comix.scrapers.bedetheque.service.FilterService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FilterController implements V1Controller, FiltersApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterController.class);

    private final FilterService filterService;

    public FilterController(FilterService filterService) {
        this.filterService = filterService;
    }

    /**
     * Global search (series, authors, graphic novels, news...)
     *
     * @param filter the information to search. (required)
     * @return the filtered data
     */
    @Override
    public ResponseEntity<GlobalFilteredDto> globalSearch(String filter) {
        LOGGER.info("Global search = {}", filter);
        return ResponseEntity.status(HttpStatus.OK).body(
                filterService.globalSearch(filter)
        );
    }

    /**
     * Graphic novels search
     *
     * @param serieId             (optional)
     * @param authorId            (optional)
     * @param serieTitle          (optional)
     * @param graphicnovelTitle   (optional)
     * @param publisher           (optional)
     * @param collection          (optional)
     * @param category            (optional)
     * @param author              (optional)
     * @param isbn                (optional)
     * @param status              (optional)
     * @param origin              (optional)
     * @param language            (optional)
     * @param keyword             (optional)
     * @param publicationDateFrom (optional)
     * @param publicationDateTo   (optional)
     * @param quotationMin        (optional)
     * @param quotationMax        (optional)
     * @param originalEdition     (optional)
     * @return the filtered graphic novels
     */
    @Override
    public ResponseEntity<GraphicNovelsFilteredDto> graphicNovelSearch(
            @Size(max = 20) @Valid @RequestParam(required = false) String serieId,
            @Size(max = 20) @Valid @RequestParam(required = false) String authorId,
            @Size(min = 3, max = 255) @Valid @RequestParam(required = false) String serieTitle,
            @Size(min = 3, max = 255) @Valid @RequestParam(required = false) String graphicnovelTitle,
            @Size(min = 3, max = 255) @Valid @RequestParam(required = false) String publisher,
            @Size(min = 3, max = 255) @Valid @RequestParam(required = false) String collection,
            @Size(min = 3, max = 255) @Valid @RequestParam(required = false) String category,
            @Size(min = 3, max = 255) @Valid @RequestParam(required = false) String author,
            @Size(min = 3, max = 30) @Valid @RequestParam(required = false) String isbn,
            @Valid @RequestParam(required = false) String status,
            @Valid @RequestParam(required = false) String origin,
            @Valid @RequestParam(required = false) String language,
            @Size(min = 3, max = 255) @Valid @RequestParam(required = false) String keyword,
            @Size(min = 3, max = 20) @Valid @RequestParam(required = false) String publicationDateFrom,
            @Size(min = 3, max = 20) @Valid @RequestParam(required = false) String publicationDateTo,
            @Size(min = 3, max = 20) @Valid @RequestParam(required = false) String quotationMin,
            @Size(min = 3, max = 20) @Valid @RequestParam(value = "qrquotationMax", required = false) String quotationMax,
            @Valid @RequestParam(required = false) String originalEdition) {
        var graphicNovelsFilters = new GraphicNovelsFilters();
        graphicNovelsFilters.setSerieId(serieId != null ? serieId : "");
        graphicNovelsFilters.setAuthorId(authorId != null ? authorId : "");
        graphicNovelsFilters.setSerieTitle(serieTitle != null ? serieTitle : "");
        graphicNovelsFilters.setGraphicnovelTitle(graphicnovelTitle != null ? graphicnovelTitle : "");
        graphicNovelsFilters.setPublisher(publisher != null ? publisher : "");
        graphicNovelsFilters.setCollection(collection != null ? collection : "");
        graphicNovelsFilters.setCategory(category != null ? category : "");
        graphicNovelsFilters.setAuthor(author != null ? author : "");
        graphicNovelsFilters.setIsbn(isbn != null ? isbn : "");
        graphicNovelsFilters.setStatus(SerieStatus.fromValue(status));
        graphicNovelsFilters.setOrigin(SerieOrigin.fromValue(origin));
        graphicNovelsFilters.setLanguage(SerieLanguage.fromValue(language));
        graphicNovelsFilters.setKeyword(keyword != null ? keyword : "");
        graphicNovelsFilters.setPublicationDateFrom(publicationDateFrom != null ? publicationDateFrom : "");
        graphicNovelsFilters.setPublicationDateTo(publicationDateTo != null ? publicationDateTo : "");
        graphicNovelsFilters.setQuotationMin(quotationMin != null ? quotationMin : "");
        graphicNovelsFilters.setQuotationMax(quotationMax != null ? quotationMax : "");
        graphicNovelsFilters.setOriginalEdition(originalEdition != null ? originalEdition : "");

        var filters = graphicNovelsFilters.toString();
        LOGGER.info("Graphic novels search = {}", filters);
        return ResponseEntity.status(HttpStatus.OK).body(
                filterService.graphicNovelSearch(graphicNovelsFilters)
        );
    }

    @Override
    public ResponseEntity<List<AutocompleteSearchDto>> autocomplete(FilterAutocompleteTypeEnumDto type, String filter) {
        return switch (type) {
            case SERIES -> ResponseEntity
                    .status(HttpStatus.OK)
                    .body(filterService.autocompleteSeries(filter));
            case PUBLISHERS -> ResponseEntity
                    .status(HttpStatus.OK)
                    .body(filterService.autocompletePublishers(filter));
            case COLLECTIONS -> ResponseEntity
                    .status(HttpStatus.OK)
                    .body(filterService.autocompleteCollections(filter));
            case AUTHORS -> ResponseEntity
                    .status(HttpStatus.OK).body(filterService.autocompleteAuthors(filter));
            case CATEGORIES -> ResponseEntity
                    .status(HttpStatus.OK)
                    .body(filterService.autocompleteCategories(filter));
        };
    }
}
