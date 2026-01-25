package com.comix.scrapers.bedetheque.client.model.graphicnovel;

import com.comix.scrapers.bedetheque.client.model.serie.Serie;
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
    private String cycle;
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

    private String backCoverOriginalUrl;
    private String backCoverUrl;
    private String backCoverPath;
    private String backCoverFilename;
    private Long backCoverFileSize;
    private Boolean backCoverAvailable;
    private String backCoverTitle;

    private String backCoverThumbnailOriginalUrl;
    private String backCoverThumbnailUrl;
    private String backCoverThumbnailPath;
    private String backCoverThumbnailFilename;
    private Long backCoverThumbnailFileSize;
    private Boolean backCoverThumbnailAvailable;
    private String backCoverThumbnailTitle;

    private String pageExampleOriginalUrl;
    private String pageExampleUrl;
    private String pageExamplePath;
    private String pageExampleFilename;
    private Long pageExampleFileSize;
    private Boolean pageExampleAvailable;
    private String pageExampleTitle;

    private String pageExampleThumbnailOriginalUrl;
    private String pageExampleThumbnailUrl;
    private String pageExampleThumbnailPath;
    private String pageExampleThumbnailFilename;
    private Long pageExampleThumbnailFileSize;
    private Boolean pageExampleThumbnailAvailable;
    private String pageExampleThumbnailTitle;

    private String copyright;
    private String scrapUrl;
    private Ratings ratings;
    private Serie serie;

    public void addAuthor(AuthorRole authorRole) {
        authors.add(authorRole);
    }
}
