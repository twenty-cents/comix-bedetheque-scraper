package com.comix.scrapers.bedetheque.rest.mapper;

import com.comix.scrapers.bedetheque.client.model.filter.AutocompleteSearch;
import com.comix.scrapers.bedetheque.client.model.filter.GlobalFilteredObject;
import com.comix.scrapers.bedetheque.client.model.filter.GraphicNovelsFilteredObject;
import com.comix.scrapers.bedetheque.rest.v1.dto.AutocompleteSearchDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.GlobalFilteredDto;
import com.comix.scrapers.bedetheque.rest.v1.dto.GraphicNovelsFilteredDto;
import org.mapstruct.Mapper;

@Mapper
public interface FilterMapper {

    GlobalFilteredDto globalFilteredObjectToGlobalFilteredDto(GlobalFilteredObject globalFilteredObject);

    GraphicNovelsFilteredDto graphicNovelsFilteredObjectToGraphicNovelsFilteredDto(GraphicNovelsFilteredObject graphicNovelsFilteredObject);

    AutocompleteSearchDto autocompleteSearchToAutocompleteSearchDto(AutocompleteSearch autocompleteSearch);
}
