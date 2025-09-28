package com.comix.scrapers.bedetheque.client.model.filter;


import com.comix.scrapers.bedetheque.client.model.serie.SerieLanguage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class GraphicNovelsFilters {

    private String serieId = "";
    private String authorId = "";
    private String serieTitle = "";
    private String graphicnovelTitle = "";
    private String publisher = "";
    private String collection = "";
    private String category = "";
    private String author = "";
    private String isbn = "";
    private SerieStatus status = SerieStatus.UNAVAILABLE;
    private SerieOrigin origin = SerieOrigin.UNAVAILABLE;
    private SerieLanguage language = SerieLanguage.UNAVAILABLE;
    private String keyword = "";
    private String publicationDateFrom = "";
    private String publicationDateTo = "";
    private String quotationMin = "";
    private String quotationMax = "";
    private String originalEdition = "";

}
