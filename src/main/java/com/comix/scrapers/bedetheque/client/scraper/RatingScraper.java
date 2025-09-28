package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.client.model.rating.Rating;
import com.comix.scrapers.bedetheque.util.HTML;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class RatingScraper extends GenericScraper {

    @Value("${application.downloads.user.avatar.thumbs}")
    private String outputUserAvatarThumbDirectory;

    @Value("${application.http.medias.user.avatar.thumbs}")
    private String httpUserAvatarThumbDirectory;

    @Value("${application.downloads.graphic-novels.cover-front.thumbs}")
    private String outputCoverFrontThumbDirectory;

    @Value("${application.http.medias.graphic-novels.cover-front.thumbs}")
    private String httpCoverFrontThumbDirectory;

    @Value("${application.http.medias.default.unavailable}")
    private String httpDefaultMediaFilename;

    @Setter
    @Value("${application.downloads.localcache.active}")
    private boolean isLocalCacheActive;

    @Setter
    @Value("#{new Long('${application.scraping.latency}')}")
    private Long latency;

    public List<Rating> scrap(String url) {
        List<Rating> ratings = new ArrayList<>();
        // Load all series starting with the letter
        var doc = GenericScraperSingleton.getInstance().load(url, latency);
        var eRatings = doc.select("ol.commentlist div.the-comment");

        for(Element r : eRatings) {
            var rating = new Rating();
            rating.setGraphicNovelTitle(getGraphicNovelTitle(r));
            rating.setGraphicNovelUrl(getGraphicNovelUrl(r));
            rating.setGraphicNovelPictureUrl(getGraphicNovelPictureUrl(r));
            rating.setGraphicNovelPictureTitle(getGraphicNovelPictureTitle(r));
            rating.setCreateBy(getCreateBy(r));
            rating.setCreateByAllRatingsUrl(getCreateByAllRatingsUrl(r));
            rating.setCreateOn(getCreateOn(r));
            rating.setRatingPictureUrl(getRatingPictureUrl(r));
            rating.setRatingTitle(getRatingTitle(r));
            rating.setComment(getComment(r));

            ratings.add(rating);
        }
        // Download all thumbs in the local server
        if(isLocalCacheActive) {
            downloadThumbs(ratings);
        }
        log.info("Scraped {} ratings from the url {}", ratings.size(), url);
        return ratings;
    }

    /**
     * Download all bedetheque medias on the local server
     * @param ratings the ratings
     */
    private void downloadThumbs(List<Rating> ratings) {
        for(Rating r : ratings) {
            String idGc = GraphicNovelScraper.scrapIdFromUrl(r.getGraphicNovelUrl());

            // Preload the graphic novel front cover page
            if(!StringUtils.isBlank(r.getGraphicNovelPictureUrl())) {
                r.setGraphicNovelPictureUrl(
                    downloadMedia(
                            outputCoverFrontThumbDirectory,
                            httpCoverFrontThumbDirectory,
                            r.getGraphicNovelPictureUrl(),
                            httpDefaultMediaFilename,
                            idGc));
            }
            // Preload the user avatar
            if(!StringUtils.isBlank(r.getRatingPictureUrl())) {
                r.setRatingPictureUrl(
                    downloadMedia(
                            outputUserAvatarThumbDirectory,
                            httpUserAvatarThumbDirectory,
                            r.getRatingPictureUrl(),
                            httpDefaultMediaFilename,
                            "1"));
            }
        }
    }

    private String getGraphicNovelTitle(Element eRating) {
        String res = null;
        try {
            res = ownText(eRating.selectFirst("div.comment-title a"));
        } catch (Exception ignored) {
            log.debug("Failed to extract graphic novel tile.");
        }
        return res;
    }

    private String getGraphicNovelUrl(Element eRating) {
        String res = null;
        try {
            res = attr(eRating.selectFirst("div.comment-title a"), HTML.Attribute.HREF);
        } catch (Exception ignored) {
            log.debug("Failed to extract graphic novel url.");
        }
        return res;
    }

    private String getGraphicNovelPictureUrl(Element eRating) {
        String res = null;
        try {
            res = attr(eRating.selectFirst("div.alignleft img"), HTML.Attribute.SRC);
        } catch (Exception ignored) {
            log.debug("Failed to extract graphic novel picture url.");
        }
        return res;
    }

    private String getGraphicNovelPictureTitle(Element eRating) {
        String res = null;
        try {
            res = attr(eRating.selectFirst("div.alignleft img"), HTML.Attribute.TITLE);
        } catch (Exception ignored) {
            log.debug("Failed to extract graphic novel picture title.");
        }
        return res;
    }

    private String getCreateBy(Element eRating) {
        String res = null;
        try {
            res = ownText(eRating.selectFirst("div.comment-author a"));
        } catch (Exception ignored) {
            log.debug("Failed to extract author's name.");
        }
        return res;
    }

    private String getCreateByAllRatingsUrl(Element eRating) {
        String res = null;
        try {
            res = attr(eRating.selectFirst("div.comment-author a"), HTML.Attribute.HREF);
        } catch (Exception ignored) {
            log.debug("Failed to extract author's all ratings url.");
        }
        return res;
    }

    private String getCreateOn(Element eRating) {
        String res = null;
        try {
            res = ownText(eRating.selectFirst("div.comment-author small"));
        } catch (Exception ignored) {
            log.debug("Failed to extract create on date.");
        }
        return res;
    }

    private String getRatingPictureUrl(Element eRating) {
        String res = null;
        try {
            res = attr(eRating.selectFirst("div.comment-author img"), HTML.Attribute.SRC);
        } catch (Exception ignored) {
            log.debug("Failed to extract rating picture url.");
        }
        return res;
    }

    private String getRatingTitle(Element eRating) {
        String res = null;
        try {
            res = attr(eRating.selectFirst("div.comment-author img"), HTML.Attribute.TITLE);
        } catch (Exception ignored) {
            log.debug("Failed to extract rating title.");
        }
        return res;
    }

    private String getComment(Element eRating) {
        String res = null;
        try {
            res = ownText(eRating.selectFirst("div.comment-text p"));
        } catch (Exception ignored) {
            log.debug("Failed to extract comment.");
        }
        return res;
    }

}
