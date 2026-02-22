package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.client.model.media.Media;
import com.comix.scrapers.bedetheque.client.model.media.MediaType;
import com.comix.scrapers.bedetheque.exception.TechnicalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MediaScraper extends GenericScraper {

    // Series
    @Value("${application.downloads.series.cover-front.thumbs}")
    private String serieOutputCoverFrontThumbDirectory;

    @Value("${application.http.medias.series.cover-front.thumbs}")
    private String serieHttpCoverFrontThumbDirectory;

    @Value("${application.downloads.series.page-example.thumbs}")
    private String serieOutputPageExampleThumbDirectory;

    @Value("${application.http.medias.series.page-example.thumbs}")
    private String serieHttpPageExampleThumbDirectory;

    @Value("${application.downloads.series.page-example.hd}")
    private String serieOutputPageExampleHdDirectory;

    @Value("${application.http.medias.series.page-example.hd}")
    private String serieHttpPageExampleHdDirectory;

    // Comic books
    @Value("${application.downloads.graphic-novels.cover-front.hd}")
    private String comicBookOutputCoverFrontHdDirectory;

    @Value("${application.http.medias.graphic-novels.cover-front.hd}")
    private String comicBookHttpCoverFrontHdDirectory;

    @Value("${application.downloads.graphic-novels.cover-back.hd}")
    private String comicBookOutputCoverBackHdDirectory;

    @Value("${application.http.medias.graphic-novels.cover-back.hd}")
    private String comicBookHttpCoverBackHdDirectory;

    @Value("${application.downloads.graphic-novels.page-example.hd}")
    private String comicBookOutputPageExampleHdDirectory;

    @Value("${application.http.medias.graphic-novels.page-example.hd}")
    private String comicBookHttpPageExampleHdDirectory;

    @Value("${application.downloads.graphic-novels.cover-front.thumbs}")
    private String comicBookOutputCoverFrontThumbDirectory;

    @Value("${application.http.medias.graphic-novels.cover-front.thumbs}")
    private String comicBookHttpCoverFrontThumbDirectory;

    @Value("${application.downloads.graphic-novels.cover-back.thumbs}")
    private String comicBookOutputCoverBackThumbDirectory;

    @Value("${application.http.medias.graphic-novels.cover-back.thumbs}")
    private String comicBookHttpCoverBackThumbDirectory;

    @Value("${application.downloads.graphic-novels.page-example.thumbs}")
    private String comicBookOutputPageExampleThumbDirectory;

    @Value("${application.http.medias.graphic-novels.page-example.thumbs}")
    private String comicBookHttpPageExampleThumbDirectory;

    // Authors
    @Value("${application.downloads.authors.photo.hd}")
    private String authorOutputAuthorHdDirectory;

    @Value("${application.http.medias.authors.photo.hd}")
    private String authorHttpAuthorHdPath;

    public Media scrap(String id, MediaType type, String originalUrl) {
        Media media = new Media();
        media.setId(id);
        media.setType(type);
        media.setOriginalUrl(originalUrl);
        media.setFilename(getMediaFilename(originalUrl));
        media.setAvailable(false);
        media.setFileSize(0L);

        String url;
        String path = switch (type) {
            case SERIE_COVER_THUMBNAIL -> {
                url = getHashedOutputMediaUrl(originalUrl, serieHttpCoverFrontThumbDirectory, id);
                yield getHashedOutputMediaPath(originalUrl, serieOutputCoverFrontThumbDirectory, id);
            }
            case SERIE_EXAMPLE_PAGE -> {
                url = getHashedOutputMediaUrl(originalUrl, serieHttpPageExampleHdDirectory, id);
                yield getHashedOutputMediaPath(originalUrl, serieOutputPageExampleHdDirectory, id);
            }
            case SERIE_EXAMPLE_PAGE_THUMBNAIL -> {
                url = getHashedOutputMediaUrl(originalUrl, serieHttpPageExampleThumbDirectory, id);
                yield getHashedOutputMediaPath(originalUrl, serieOutputPageExampleThumbDirectory, id);
            }
            case COMIC_BOOK_COVER -> {
                url = getHashedOutputMediaUrl(originalUrl, comicBookHttpCoverFrontHdDirectory, id);
                yield getHashedOutputMediaPath(originalUrl, comicBookOutputCoverFrontHdDirectory, id);
            }
            case COMIC_BOOK_COVER_THUMBNAIL -> {
                url = getHashedOutputMediaUrl(originalUrl, comicBookHttpCoverFrontThumbDirectory, id);
                yield getHashedOutputMediaPath(originalUrl, comicBookOutputCoverFrontThumbDirectory, id);
            }
            case COMIC_BOOK_BACKCOVER -> {
                url = getHashedOutputMediaUrl(originalUrl, comicBookHttpCoverBackHdDirectory, id);
                yield getHashedOutputMediaPath(originalUrl, comicBookOutputCoverBackHdDirectory, id);
            }
            case COMIC_BOOK_BACKCOVER_THUMBNAIL -> {
                url = getHashedOutputMediaUrl(originalUrl, comicBookHttpCoverBackThumbDirectory, id);
                yield getHashedOutputMediaPath(originalUrl, comicBookOutputCoverBackThumbDirectory, id);
            }
            case COMIC_BOOK_EXAMPLE_PAGE -> {
                url = getHashedOutputMediaUrl(originalUrl, comicBookHttpPageExampleHdDirectory, id);
                yield getHashedOutputMediaPath(originalUrl, comicBookOutputPageExampleHdDirectory, id);
            }
            case COMIC_BOOK_EXAMPLE_PAGE_THUMBNAIL -> {
                url = getHashedOutputMediaUrl(originalUrl, comicBookHttpPageExampleThumbDirectory, id);
                yield getHashedOutputMediaPath(originalUrl, comicBookOutputPageExampleThumbDirectory, id);
            }
            case AUTHOR_PHOTO -> {
                url = getHashedOutputMediaUrl(originalUrl, authorHttpAuthorHdPath, id);
                yield getHashedOutputMediaPath(originalUrl, authorOutputAuthorHdDirectory, id);
            }
            default -> {
                url = null;
                yield null;
            }
        };

        media.setUrl(url);
        media.setPath(path);

        // Download media
        download(media);

        return media;
    }

    /**
     * Download media in the NFS server
     *
     * @param media the media to download
     */
    void download(Media media) {
        try {
            download(media.getOriginalUrl(), media.getPath());
            media.setAvailable(true);
            media.setFileSize(getMediaSize(media.getPath()));
        } catch (TechnicalException e) {
            media.setAvailable(false);
            media.setFileSize(0L);
            log.error(e.getMessage(), e);
        }
    }
}
