package com.comix.scrapers.bedetheque.client.model.graphicnovel;

import lombok.*;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(exclude = "graphicNovels")
@ToString(exclude = "graphicNovels")
@NoArgsConstructor
@AllArgsConstructor
public class GraphicNovelPage {

    private Integer page;
    private Integer size;
    private Integer totalPages;
    private Integer totalElements;
    private List<GraphicNovel> graphicNovels;
}
