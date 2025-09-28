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
    private String coverUrl;
    private String coverTitle;
    // FreeBds attributes
    private Boolean isInCollection;

    public int compareTo(SerieToDiscover s2){
        if(title.compareTo(s2.title) == 0) {
            return 0;
        } else if(title.compareTo(s2.title) > 0) {
            return 1;
        } else {
            return -1;
        }
    }
}
