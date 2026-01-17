package com.comix.scrapers.bedetheque.client.model.serie;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ToReadSerie {

    private String externalId;
    private String title;
    private String url;
    private String originalCoverUrl;
    private String coverUrl;
    private String coverPath;
    private String coverFilename;
    private Long coverFileSize;
    private Boolean isCoverChecked;
    private String coverTitle;
}
