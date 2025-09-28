package com.comix.scrapers.bedetheque.client.model.statistics;

import lombok.Data;

@Data
public class LastEntry {

    private String id;
    private String tome;
    private String title;
    private String serieTitle;
    private String graphicNovelUrl;
    private String frontCoverThumbnailUrl;
    private String frontCoverHdUrl;
    private String publisher;
    private String publicationDate;
    private String synopsys;
}
