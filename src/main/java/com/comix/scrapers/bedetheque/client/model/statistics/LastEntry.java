package com.comix.scrapers.bedetheque.client.model.statistics;

import lombok.Data;

@Data
public class LastEntry {

    private String id;
    private String tome;
    private String title;
    private String serieTitle;
    private String graphicNovelUrl;
    private String coverOriginalUrl;
    private String coverUrl;
    private String coverPath;
    private String coverFilename;
    private Long coverFileSize;
    private Boolean coverAvailable;
    private String coverTitle;
    private String coverThumbnailOriginalUrl;
    private String coverThumbnailUrl;
    private String coverThumbnailPath;
    private String coverThumbnailFilename;
    private Long coverThumbnailFileSize;
    private Boolean coverThumbnailAvailable;
    private String coverThumbnailTitle;
    private String publisher;
    private String publicationDate;
    private String synopsys;
}
