package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.client.model.statistics.GlobalStatistics;
import com.comix.scrapers.bedetheque.client.model.statistics.LastEntry;
import com.comix.scrapers.bedetheque.util.HTML;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class GlobalStatisticsScraper extends GenericScraper {

    @Setter
    @Value("${bedetheque.url.home}")
    private String bedethequeUrl;

    // Thumbnails medias
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

    public GlobalStatistics scrap() {
        Document doc = GenericScraperSingleton.getInstance().load(bedethequeUrl, latency);
        GlobalStatistics globalStatistics = new GlobalStatistics();
        globalStatistics.setSeries(scrapElement(doc, "ul.stats li:contains(Séries) > span"));
        globalStatistics.setGraphicNovels(scrapElement(doc, "ul.stats li:contains(Albums) > span"));
        globalStatistics.setAuthors(scrapElement(doc, "ul.stats li:contains(Auteurs) > span"));
        globalStatistics.setReviews(scrapElement(doc, "ul.stats li:contains(Revues) > span"));
        globalStatistics.setNews(scrapNews(doc));
        globalStatistics.setLastEntries(scrapLastEntries(doc));

        return globalStatistics;
    }

    private List<LastEntry> scrapNews(Document doc) {
        List<LastEntry> graphicNovels = new ArrayList<>();
        // First new graphic novel
        Optional<LastEntry> optionalLastEntry = scrapNewFirst(doc);
        optionalLastEntry.ifPresent(graphicNovels::add);
        // Other news
        List<Element> lis = doc.select("ul.gallery-couv-large li");
        for (Element li : lis) {
            graphicNovels.add(scrapNew(li));
        }
        return graphicNovels;
    }

    private Optional<LastEntry> scrapNewFirst(Document doc) {
        Optional<LastEntry> optionalLastEntry = Optional.empty();
        Element bloc = doc.selectFirst("div.magazine-widget");
        if (bloc == null) {
            return optionalLastEntry;
        }

        String graphicNovelUrl = bedethequeUrl + "/" + attr(bloc.selectFirst("a"), HTML.Attribute.HREF);
        String frontCoverHdUrl = bedethequeUrl + "/" + attr(bloc.selectFirst("img"), HTML.Attribute.SRC);

        Element blocDescription = bloc.selectFirst("div.big-desc");
        String title = null;
        String tome = null;
        String serieTitle = null;
        String publisher = null;
        String publicationDate = null;
        String synopsys = null;
        // Scrap title
        if (blocDescription != null) {
            String titleH4 = ownText(blocDescription.selectFirst("h4"));
            title = titleH4;
            // Search if a tome number is specified
            if (!StringUtils.isBlank(titleH4)) {
                String[] parts = StringUtils.split(titleH4, ".");
                if (parts.length > 1 && StringUtils.isNumeric(parts[0])) {
                    tome = parts[0];
                    title = Strings.CS.remove(titleH4, parts[0] + ". ").trim();
                }
                // Scrap serie title
                serieTitle = ownText(blocDescription.selectFirst("h3 > a"));
            }

            String serieDetails = ownText(blocDescription.selectFirst("div.magz-meta"));
            if (!StringUtils.isBlank(serieDetails)) {
                // Scrap publisher
                publisher = scrapLastEntryPublisher(serieDetails);
                // Scrap publication date
                publicationDate = scrapLastEntryPublicationDate(serieDetails);
            }

            // Scrap synopsys
            synopsys = scrapLastEntrySynopsys(blocDescription);

            // Download all thumbs in the local server
            frontCoverHdUrl = scrapLastEntryFrontCoverHdUrl(frontCoverHdUrl, GraphicNovelScraper.scrapIdFromUrl(graphicNovelUrl));
        }

        LastEntry lastEntry = new LastEntry();
        lastEntry.setId(GraphicNovelScraper.scrapIdFromUrl(graphicNovelUrl));
        lastEntry.setTome(tome);
        lastEntry.setTitle(title);
        lastEntry.setSerieTitle(serieTitle);
        lastEntry.setGraphicNovelUrl(graphicNovelUrl);
        lastEntry.setFrontCoverHdUrl(frontCoverHdUrl);
        lastEntry.setPublisher(publisher);
        lastEntry.setPublicationDate(publicationDate);
        lastEntry.setSynopsys(synopsys);
        optionalLastEntry = Optional.of(lastEntry);

        return optionalLastEntry;
    }

    private String scrapLastEntryPublisher(String serieDetails) {
        String publisher = null;
        String[] parts = StringUtils.split(serieDetails, "|");
        // Scrap publisher
        if (parts.length > 0) {
            publisher = parts[0];
        }
        return publisher;
    }

    private String scrapLastEntryPublicationDate(String serieDetails) {
        String publicationDate = null;
        String[] parts = StringUtils.split(serieDetails, "|");
        // Scrap publication date
        if (parts.length > 1) {
            publicationDate = parts[1];
        }
        return publicationDate;
    }

    /**
     * Scrap Last entry synopsys
     *
     * @param blocDescription the bloc description to scrap
     * @return a synopsys if exists
     */
    private String scrapLastEntrySynopsys(Element blocDescription) {
        String synopsys = ownText(blocDescription.selectFirst("p"));
        if (!synopsys.isEmpty()) {
            synopsys += " [...]";
        }
        return synopsys;
    }

    /**
     * Scrap Last entry front cover HD url
     *
     * @param frontCoverHdUrl the front cover HD url to scrap
     * @param id the graphic novel id
     * @return a front cover HD url if exists
     */
    private String scrapLastEntryFrontCoverHdUrl(String frontCoverHdUrl, String id) {
        if (isLocalCacheActive) {
            frontCoverHdUrl = downloadThumb(frontCoverHdUrl, id);
        }
        return frontCoverHdUrl;
    }

    private LastEntry scrapNew(Element li) {
        String graphicNovelUrl = bedethequeUrl + "/" + attr(li.selectFirst("a"), HTML.Attribute.HREF);
        String frontCoverThumbnailUrl = bedethequeUrl + "/" + attr(li.selectFirst("img"), HTML.Attribute.SRC);
        String serieTitle = attr(li.selectFirst("a"), HTML.Attribute.TITLE);
        String title = serieTitle;
        String publisher = ownText(li.selectFirst("span.editeur"));
        String tome = ownText(li.selectFirst("span.titre"));
        // TODO Limit case : example -> La ferme de l'enfant-loup
        if (StringUtils.split(tome, "-").length > 1) {
            String part = StringUtils.split(tome, "-")[1];
            if (part.length() > 6) {
                tome = StringUtils.substring(part, 6).trim();
                String t = tome + ". ";
                String[] parts = serieTitle.split(t);
                if (parts.length > 1) {
                    title = parts[parts.length - 1].trim();
                }
                // Remove graphic novel title from the serie title
                serieTitle = serieTitle.split(tome)[0].trim();
            } else {
                tome = null;
            }
        } else {
            tome = null;
        }
        // Download all thumbs in the local server
        if (isLocalCacheActive) {
            frontCoverThumbnailUrl = downloadThumb(frontCoverThumbnailUrl, GraphicNovelScraper.scrapIdFromUrl(graphicNovelUrl));
        }

        LastEntry lastEntry = new LastEntry();
        lastEntry.setId(GraphicNovelScraper.scrapIdFromUrl(graphicNovelUrl));
        lastEntry.setTome(tome);
        lastEntry.setTitle(title);
        lastEntry.setSerieTitle(serieTitle);
        lastEntry.setGraphicNovelUrl(graphicNovelUrl);
        lastEntry.setFrontCoverThumbnailUrl(frontCoverThumbnailUrl);
        lastEntry.setPublisher(publisher);

        return lastEntry;
    }

    private List<LastEntry> scrapLastEntries(Document doc) {
        List<LastEntry> graphicNovels = new ArrayList<>();
        List<Element> lis = doc.select("ul.gallery-couv li");
        for (Element li : lis) {
            graphicNovels.add(scrapLastEntry(li));
        }
        return graphicNovels;
    }

    private LastEntry scrapLastEntry(Element li) {
        String graphicNovelUrl = bedethequeUrl + "/" + attr(li.selectFirst("a"), HTML.Attribute.HREF);
        String frontCoverThumbnailUrl = bedethequeUrl + "/" + attr(li.selectFirst("img"), HTML.Attribute.SRC);
        String title = ownText(li.selectFirst("b"));
        String tome = ownText(li.selectFirst("span"));
        if (!StringUtils.isBlank(tome) && tome.length() > 6) {
            tome = StringUtils.substring(tome, 6).trim();
        }
        String id = GraphicNovelScraper.scrapIdFromUrl(graphicNovelUrl);
        // Download all thumbs in the local server
        if (isLocalCacheActive) {
            frontCoverThumbnailUrl = downloadThumb(frontCoverThumbnailUrl, id);
        }

        LastEntry lastEntry = new LastEntry();
        lastEntry.setId(id);
        lastEntry.setSerieTitle(title);
        lastEntry.setTome(tome);
        lastEntry.setGraphicNovelUrl(graphicNovelUrl);
        lastEntry.setFrontCoverThumbnailUrl(frontCoverThumbnailUrl);
        return lastEntry;
    }

    /**
     * Scrap an unique value from the DOM
     *
     * @param doc   the document to scrap
     * @param query the query to apply to extract values
     * @return a scraped value
     */
    private Integer scrapElement(Document doc, String query) {
        var element = doc.selectFirst(query);
        if (element != null) {
            var res = element.ownText().trim();
            return Integer.parseInt(StringUtils.deleteWhitespace(res));
        }
        return Integer.getInteger("0");
    }

    private String downloadThumb(String frontCoverThumbnail, String id) {
        if (frontCoverThumbnail.compareTo("") > 0) {
            frontCoverThumbnail = downloadMedia(outputCoverFrontThumbDirectory, httpCoverFrontThumbDirectory,
                    frontCoverThumbnail, httpDefaultMediaFilename, id);
        }
        return frontCoverThumbnail;
    }
}
