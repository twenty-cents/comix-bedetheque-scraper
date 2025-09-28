package com.comix.scrapers.bedetheque.client.model.graphicnovel;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(exclude = "authors")
@ToString(exclude = "authors")
@NoArgsConstructor
@AllArgsConstructor
public class GraphicNovel {

    private String externalId;
    private String tome;
    private Integer tomeNum;
    private String numEdition;
    private String title;
    private List<AuthorRole> authors = new ArrayList<>();
    private String publicationDate;
    private String releaseDate;
    private String publisher;
    private String collection;
    private String collectionUrl;
    private String isbn;
    private Integer totalPages;
    private String format;
    private Boolean isOriginalPublication;
    private Boolean isIntegrale;
    private Boolean isBroche;
    private String infoEdition;
    private String reeditionUrl;
    private String reeditionCount;
    private String externalIdOriginalPublication;
    private String coverPictureUrl;
    private String coverThumbnailUrl;
    private String backCoverPictureUrl;
    private String backCoverThumbnailUrl;
    private String pagePictureUrl;
    private String pageThumbnailUrl;
    private String copyright;
    private String scrapUrl;
    private Ratings ratings;

    public void addAuthor(AuthorRole authorRole) {
        authors.add(authorRole);
    }
}
