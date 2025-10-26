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
    private String coverUrl;
    private String coverTitle;
}
