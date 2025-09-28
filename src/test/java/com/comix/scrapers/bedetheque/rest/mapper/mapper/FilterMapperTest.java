package com.comix.scrapers.bedetheque.rest.mapper.mapper;

import com.comix.scrapers.bedetheque.client.model.filter.*;
import com.comix.scrapers.bedetheque.rest.mapper.FilterMapper;
import com.comix.scrapers.bedetheque.rest.v1.dto.AutocompleteSearchDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.GlobalFilteredDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.GraphicNovelsFilteredDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FilterMapperTest {

    // On récupère une instance de l'implémentation du mapper générée par MapStruct
    private final FilterMapper filterMapper = Mappers.getMapper(FilterMapper.class);

    @Test
    @DisplayName("Should map GlobalFilteredObject to GlobalFilteredDto correctly")
    void shouldMapGlobalFilteredObjectToDto() {
        // GIVEN: un objet source avec des données de test
        GlobalFilteredObject source = new GlobalFilteredObject(
                "filter",
                "",
                List.of(new FilteredChronicle()),
                "",
                List.of(new FilteredNews()),
                "",
                List.of(new FilteredPreview()),
                "",
                List.of(new FilteredAuthor()),
                "",
                List.of(new FilteredSerie()),
                "",
                List.of(new FilteredSerie()),
                "",
                List.of(new FilteredGraphicNovel()),
                ""
        );

        // WHEN: on appelle la méthode de mapping
        GlobalFilteredDto destination = filterMapper.globalFilteredObjectToGlobalFilteredDto(source);

        // THEN: on vérifie que l'objet de destination est correct
        assertThat(destination).isNotNull();
        assertThat(destination.getFilteredSeries()).hasSize(1);
        assertThat(destination.getFilteredAuthors()).hasSize(1);
        assertThat(destination.getFilteredGraphicNovels()).hasSize(1);
    }

    @Test
    @DisplayName("Should return null when GlobalFilteredObject source is null")
    void shouldReturnNullForNullGlobalFilteredObject() {
        // WHEN
        GlobalFilteredDto destination = filterMapper.globalFilteredObjectToGlobalFilteredDto(null);

        // THEN
        assertThat(destination).isNull();
    }

    @Test
    @DisplayName("Should map GraphicNovelsFilteredObject to GraphicNovelsFilteredDto correctly")
    void shouldMapGraphicNovelsFilteredObjectToDto() {
        // GIVEN
        GraphicNovelsFilteredObject source = new GraphicNovelsFilteredObject(
                "filter",
                List.of(new FilteredGraphicNovelDetails()),
                ""
        );

        // WHEN
        GraphicNovelsFilteredDto destination = filterMapper.graphicNovelsFilteredObjectToGraphicNovelsFilteredDto(source);

        // THEN
        assertThat(destination).isNotNull();
        assertThat(destination.getFilteredGraphicNovelDetails()).hasSize(1);
    }

    @Test
    @DisplayName("Should return null when GraphicNovelsFilteredObject source is null")
    void shouldReturnNullForNullGraphicNovelsFilteredObject() {
        // WHEN
        GraphicNovelsFilteredDto destination = filterMapper.graphicNovelsFilteredObjectToGraphicNovelsFilteredDto(null);

        // THEN
        assertThat(destination).isNull();
    }

    @Test
    @DisplayName("Should map AutocompleteSearch to AutocompleteSearchDto correctly")
    void shouldMapAutocompleteSearchToDto() {
        // GIVEN
        AutocompleteSearch source = new AutocompleteSearch("id", "Test Label", "Test Value", null);

        // WHEN
        AutocompleteSearchDto destination = filterMapper.autocompleteSearchToAutocompleteSearchDto(source);

        // THEN
        assertThat(destination).isNotNull();
        assertThat(destination.getLabel()).isEqualTo(source.getLabel());
        assertThat(destination.getValue()).isEqualTo(source.getValue());
    }

    @Test
    @DisplayName("Should return null when AutocompleteSearch source is null")
    void shouldReturnNullForNullAutocompleteSearch() {
        // WHEN
        AutocompleteSearchDto destination = filterMapper.autocompleteSearchToAutocompleteSearchDto(null);

        // THEN
        assertThat(destination).isNull();
    }
}