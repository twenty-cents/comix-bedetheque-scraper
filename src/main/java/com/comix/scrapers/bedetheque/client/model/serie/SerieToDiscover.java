package com.comix.scrapers.bedetheque.client.model.serie;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SerieToDiscover implements Comparable<SerieToDiscover> {

    private String id;
    private String title;
    private String url;
    private String originalCoverUrl;
    private String coverUrl;
    private String coverPath;
    private String coverFilename;
    private Long coverFileSize;
    private Boolean isCoverChecked;
    private String coverTitle;

    public int compareTo(SerieToDiscover s2){
        return Integer.compare(title.compareTo(s2.title), 0);
    }
}
