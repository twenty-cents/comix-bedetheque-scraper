package com.comix.scrapers.bedetheque.client.model.graphicnovel;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GraphicNovelSideListItem {

    private String id;
    private String tome;
    private String numEdition;
    private String title;
    private String publicationDate;
    private String url;
}
