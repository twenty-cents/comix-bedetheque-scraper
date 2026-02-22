package com.comix.scrapers.bedetheque.client.model.media;

public enum MediaType {

    SERIE_COVER_THUMBNAIL,
    SERIE_EXAMPLE_PAGE,
    SERIE_EXAMPLE_PAGE_THUMBNAIL,
    COMIC_BOOK_COVER,
    COMIC_BOOK_COVER_THUMBNAIL,
    COMIC_BOOK_BACKCOVER,
    COMIC_BOOK_BACKCOVER_THUMBNAIL,
    COMIC_BOOK_EXAMPLE_PAGE,
    COMIC_BOOK_EXAMPLE_PAGE_THUMBNAIL,
    AUTHOR_PHOTO;


    public static MediaType fromValue(String t) {
        for(MediaType s : MediaType.values()) {
            if(s.name().equals(t)) {
                return s;
            }
        }
        return null;
    }
}
