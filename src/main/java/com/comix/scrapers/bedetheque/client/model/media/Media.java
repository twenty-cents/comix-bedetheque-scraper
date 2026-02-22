package com.comix.scrapers.bedetheque.client.model.media;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Media {

    private String id;
    private MediaType type;
    private String originalUrl;
    private String url;
    private String path;
    private String filename;
    private Long fileSize;
    private Boolean available;
    private String title;
}
