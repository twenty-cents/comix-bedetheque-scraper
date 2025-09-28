package com.comix.scrapers.bedetheque.client.model.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class FilteredGraphicNovelDetails {

    private String flagUrl;
    private String serieTitle;
    private String tome;
    private String numEdition;
    private String title;
    private String coverUrl;
    private String publicationDate;
    private String url;
}
