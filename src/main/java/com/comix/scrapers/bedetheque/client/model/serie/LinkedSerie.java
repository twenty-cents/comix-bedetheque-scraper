package com.comix.scrapers.bedetheque.client.model.serie;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LinkedSerie {

    private String externalId;
    private String title;
    private String url;
    // FreeBds attributes
    private Boolean isInCollection;
}
