package com.comix.scrapers.bedetheque.rest.mapper;

import com.comix.scrapers.bedetheque.client.model.graphicnovel.GraphicNovel;
import com.comix.scrapers.bedetheque.rest.v1.dto.GraphicNovelDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface GraphicNovelMapper {

    /**
     * Convert a GraphicNovel to a GraphicNovelDto
     *
     * @param graphicNovel The GraphicNovel to convert
     * @return The GraphicNovelDto
     */
    GraphicNovelDto graphicNovelToGraphicNovelDto(GraphicNovel graphicNovel);

    /**
     * Convert a list of GraphicNovels to a list of GraphicNovelsDto
     *
     * @param graphicNovel The list of GraphicNovels to convert
     * @return The list of GraphicNovelsDto
     */
    List<GraphicNovelDto> graphicNovelToGraphicNovelsDto(List<GraphicNovel> graphicNovel);
}
