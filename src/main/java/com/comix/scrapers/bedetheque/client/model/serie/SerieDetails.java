package com.comix.scrapers.bedetheque.client.model.serie;

import com.comix.scrapers.bedetheque.client.model.graphicnovel.GraphicNovel;
import com.comix.scrapers.bedetheque.client.model.graphicnovel.GraphicNovelSideListItem;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(exclude = "linkedSeries, toReadSeries, graphicNovelSideList, graphicNovels")
@ToString(exclude = "linkedSeries, toReadSeries, graphicNovelSideList, graphicNovels")
@NoArgsConstructor
@AllArgsConstructor
public class SerieDetails {

    private String externalId;
    private String title;
    private String category;
    private String status;
    private String origin;
    private String language;
    private String synopsys;

    private String originalPictureUrl;
    private String pictureUrl;
    private String picturePath;
    private String pictureFilename;
    private Long pictureFileSize;
    private Boolean isPictureChecked;

    private String originalPictureThbUrl;
    private String pictureThbUrl;
    private String pictureThbPath;
    private String pictureThbFilename;
    private Long pictureThbFileSize;
    private Boolean isPictureThbChecked;

    private String scrapUrl;
    private Integer graphicNovelCount;
    private String period;
    private Integer periodFrom;
    private Integer periodTo;
    private String siteUrl;
    private String copyright;
    private Serie nextSerie;
    private Serie previousSerie;
    private Integer tomeCount;
    // Pagination
    private SeriePagination seriePagination;

    private SerieRatings ratings;
    private String linkedSeriesPictureUrl;
    private List<LinkedSerie> linkedSeries;
    private List<ToReadSerie> toReadSeries;

    private List<GraphicNovelSideListItem> graphicNovelSideList;
    private List<GraphicNovel> graphicNovels = new ArrayList<>();

    public void addGraphicNovel(GraphicNovel graphicNovel) {
        graphicNovels.add(graphicNovel);
    }

}
