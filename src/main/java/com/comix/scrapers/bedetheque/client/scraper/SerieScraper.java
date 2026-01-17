package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.client.model.graphicnovel.GraphicNovel;
import com.comix.scrapers.bedetheque.client.model.graphicnovel.GraphicNovelSideListItem;
import com.comix.scrapers.bedetheque.client.model.serie.*;
import com.comix.scrapers.bedetheque.exception.TechnicalException;
import com.comix.scrapers.bedetheque.util.HTML;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class SerieScraper extends GenericScraper {

    private final GraphicNovelScraper graphicNovelScraper;

    @Setter
    @Value("${bedetheque.url.series.index-by-letter}")
    private String bedethequeSerieIndexByLetterUrl;

    @Setter
    @Value("${bedetheque.url.serie.prefix}")
    private String bedethequeSeriePrefixUrl;

    @Value("${application.downloads.series.cover-front.thumbs}")
    private String outputCoverFrontThumbDirectory;

    @Value("${application.http.medias.series.cover-front.thumbs}")
    private String httpCoverFrontThumbDirectory;

    @Value("${application.downloads.series.page-example.thumbs}")
    private String outputPageExampleThumbDirectory;

    @Value("${application.http.medias.series.page-example.thumbs}")
    private String httpPageExampleThumbDirectory;

    @Value("${application.http.medias.default.unavailable}")
    private String httpDefaultMediaFilename;

    @Value("${application.downloads.series.page-example.hd}")
    private String outputPageExampleHdDirectory;

    @Value("${application.http.medias.series.page-example.hd}")
    private String httpPageExampleHdDirectory;

    @Setter
    @Value("${application.downloads.localcache.active}")
    private boolean isLocalCacheActive;

    @Setter
    @Value("#{new Long('${application.scraping.latency}')}")
    private Long latency;

    public SerieScraper(GraphicNovelScraper graphicNovelScraper) {
        this.graphicNovelScraper = graphicNovelScraper;
    }

    /**
     * List all existing series from <a href="http://www.bedetheque.com">...</a>
     *
     * @return the url list of all found series
     */
    public List<SeriesByLetter> listAllSeriesIndexes() {
        // Get all existing authors urls from http://www.bedetheque.com
        List<SeriesByLetter> seriesByLetters = new ArrayList<>();
        String[] letters = "0-A-B-C-D-E-F-G-H-I-J-K-L-M-N-O-P-Q-R-S-T-U-V-W-X-Y-Z".split("-");
        for(String letter : letters){
            var seriesByLetter = new SeriesByLetter();
            seriesByLetter.setLetter(letter);
            seriesByLetter.setUrl(bedethequeSerieIndexByLetterUrl.formatted(letter));
            seriesByLetters.add(seriesByLetter);
        }

        log.info("Scraped {} lists of indexed series.", seriesByLetters.size());
        return seriesByLetters;
    }

    /**
     * List all series starting with the letter
     *
     * @param letter the series starting letter to get
     * @return the series collection
     */
    public List<Serie> listByLetter(String letter) {
        List<Serie> series = new ArrayList<>();

        // Load all series starting with the letter
        Document doc = GenericScraperSingleton.getInstance().load(bedethequeSerieIndexByLetterUrl.formatted(letter), latency);

        // Retrieve all series from the html page
        var links = doc.getElementsByTag("a");
        for (Element link : links) {
            String linkHref = link.attr("href");
            if (linkHref.contains(bedethequeSeriePrefixUrl) &&
                    !(link.text().startsWith("(AUT") || link.text().startsWith("(DOC)") || link.text().startsWith("(Catalogues)"))) {
                // Update serie url to return the whole graphic novels
                series.add(new Serie(this.getIdBel(linkHref), link.text(), linkHref));
            }
        }

        log.info("Scraped {} series whose name starting with {}", series.size(), letter);
        return series;
    }

    public String getUrlWithAllGraphicNovels(String url) {
        return url.replace(".html", "__10000.html");
    }

    /**
     * Scrap a serie
     *
     * @param url the url of the serie to scrap
     * @return the scraped serie
     */
    public SerieDetails scrap(String url) {
        Document doc = GenericScraperSingleton.getInstance().load(url, latency);

        String period = retrievePeriod(doc);

        var serieDetails = new SerieDetails();
        serieDetails.setExternalId(retrieveExternalId(doc));
        serieDetails.setTitle(retrieveTitle(doc));
        serieDetails.setCategory(retrieveCategory(doc));
        serieDetails.setStatus(retrieveStatus(doc));
        serieDetails.setOrigin(retrieveOrigin(doc));
        serieDetails.setLanguage(retrieveLanguage(doc));
        serieDetails.setSiteUrl(retrieveSiteUrl(doc));
        serieDetails.setSynopsys(retrieveSynopsys(doc));
        serieDetails.setOriginalPictureUrl(retrievePictureUrl(doc));
        serieDetails.setOriginalPictureThbUrl(retrieveThumbnailUrl(doc));
        serieDetails.setCopyright(retrieveCopyright(doc));
        serieDetails.setScrapUrl(url);
        serieDetails.setGraphicNovels(retrieveGraphicNovels(url, doc));
        serieDetails.setGraphicNovelCount(retrieveGraphicNovelCount(doc));
        serieDetails.setPeriod(period);
        if(!StringUtils.isBlank(period)) {
            serieDetails.setPeriodFrom(retrievePeriodFrom(period));
            serieDetails.setPeriodTo(retrievePeriodTo(period));
        }
        serieDetails.setNextSerie(retrieveNextSerie(doc));
        serieDetails.setPreviousSerie(retrievePreviousSerie(doc));
        serieDetails.setSeriePagination(retrievePagination(doc));
        serieDetails.setGraphicNovelSideList(retrieveGraphicNovelSideList(doc));
        serieDetails.setTomeCount(retrieveTomeCount(doc));
        serieDetails.setRatings(retrieveRatings(doc));
        serieDetails.setLinkedSeriesPictureUrl(retrieveLinkedSeriesPictureUrl(doc));
        serieDetails.setLinkedSeries(retrieveLinkedSeries(doc));
        serieDetails.setToReadSeries(retrieveToReadSeries(doc));

        // Serie page example thumbnail
        if (!StringUtils.isBlank(serieDetails.getOriginalPictureThbUrl())) {
            serieDetails.setPictureThbFilename(getMediaFilename(serieDetails.getOriginalPictureThbUrl()));
            serieDetails.setPictureThbUrl(getHashedOutputMediaUrl(serieDetails.getOriginalPictureThbUrl(), httpPageExampleThumbDirectory, serieDetails.getExternalId()));
            serieDetails.setPictureThbPath(getHashedOutputMediaPath(serieDetails.getOriginalPictureThbUrl(), outputPageExampleThumbDirectory, serieDetails.getExternalId()));
            serieDetails.setIsPictureThbChecked(false);
            serieDetails.setPictureThbFileSize(0L);
        }

        // Serie page example
        if (!StringUtils.isBlank(serieDetails.getOriginalPictureUrl())) {
            serieDetails.setPictureFilename(getMediaFilename(serieDetails.getOriginalPictureUrl()));
            serieDetails.setPictureUrl(getHashedOutputMediaUrl(serieDetails.getOriginalPictureUrl(), httpPageExampleHdDirectory, serieDetails.getExternalId()));
            serieDetails.setPicturePath(getHashedOutputMediaPath(serieDetails.getOriginalPictureUrl(), outputPageExampleHdDirectory, serieDetails.getExternalId()));
            serieDetails.setIsPictureChecked(false);
            serieDetails.setPictureFileSize(0L);
        }

        // Download all thumbs in the local server
        if(isLocalCacheActive) {
            downloadExamplePageThumbnail(serieDetails);
            downloadExamplePage(serieDetails);
            downloadToReadSeriesCovers(serieDetails);
        }
        log.info("Scraped {} graphic novels from the serie url {}",
                serieDetails.getGraphicNovels().size(),
                url);
        return serieDetails;
    }

    public List<GraphicNovel> scrapGraphicNovels(String url) {
        Document doc = GenericScraperSingleton.getInstance().load(url, latency);

        String msg = "Série à scraper=" + url;
        log.info(msg);
        return retrieveGraphicNovels(url, doc);
    }

    /**
     * Download series to read (thumbnails) in the NFS server
     *
     * @param serieDetails the serie
     */
    void downloadToReadSeriesCovers(SerieDetails serieDetails) {
        for(ToReadSerie s : serieDetails.getToReadSeries()) {
            if(!StringUtils.isBlank(s.getOriginalCoverUrl())) {
                try {
                    download(s.getOriginalCoverUrl(), s.getCoverPath());
                    s.setIsCoverChecked(true);
                    s.setCoverFileSize(getMediaSize(s.getCoverPath()));
                } catch (TechnicalException e) {
                    s.setIsCoverChecked(false);
                    s.setCoverFileSize(0L);
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Download serie's example page thumbnail on the local server
     * @param serieDetails the serie
     */
    void downloadExamplePageThumbnail(SerieDetails serieDetails) {
        if (!StringUtils.isBlank(serieDetails.getOriginalPictureThbUrl())) {
            try {
                download(serieDetails.getOriginalPictureThbUrl(), serieDetails.getPictureThbPath());
                serieDetails.setIsPictureThbChecked(true);
                serieDetails.setPictureThbFileSize(getMediaSize(serieDetails.getPictureThbPath()));
            } catch (TechnicalException e) {
                serieDetails.setIsPictureThbChecked(false);
                serieDetails.setPictureThbFileSize(0L);
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Download serie's example page on the local server
     * @param serieDetails the serie
     */
    void downloadExamplePage(SerieDetails serieDetails) {
        if (!StringUtils.isBlank(serieDetails.getOriginalPictureUrl())) {
            try {
                download(serieDetails.getOriginalPictureUrl(), serieDetails.getPicturePath());
                serieDetails.setIsPictureChecked(true);
                serieDetails.setPictureFileSize(getMediaSize(serieDetails.getPicturePath()));
            } catch (TechnicalException e) {
                serieDetails.setIsPictureChecked(false);
                serieDetails.setPictureFileSize(0L);
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Scrap all graphic novels of the current serie
     *
     * @param doc the html fragment of the graphic novel to scrap
     * @return the scraped graphic novel
     */
    private List<GraphicNovel> retrieveGraphicNovels(String url, Document doc) {
        List<GraphicNovel> graphicNovels = new ArrayList<>();

        Elements eAlbums = doc.select("ul.liste-albums li[itemtype='https://schema.org/Book']");

        for(Element li : eAlbums) {
            graphicNovels.add(graphicNovelScraper.scrapFromSerie(url, doc, li));
        }

        return graphicNovels;
    }

    /**
     * Retrieve the external id of the current serie
     *
     * @param doc the fragment html of the current serie
     * @return the scraped external id
     */
    private String retrieveExternalId(Document doc) {
        String res = null;
        try {
            res = ownText(doc.select("ul.serie-info li:contains(Identifiant)").first());
        } catch (Exception ignored) {
            log.debug("Failed to extract external id.");
        }
        return res;
    }

    /**
     * Retrieve the title of the current serie
     *
     * @param doc the fragment html of the current serie
     * @return the scraped title
     */
    private String retrieveTitle(Document doc) {
        String res = null;
        try {
            res = text(doc.getElementsByTag("h1").first());
        } catch (Exception ignored) {
            log.debug("Failed to extract title.");
        }
        return res;
    }

    /**
     * Retrieve the category of the current serie
     *
     * @param doc the fragment html of the current serie
     * @return the scraped category
     */
    private String retrieveCategory(Document doc) {
        String res = null;
        try {
            res = ownText(doc.select("ul.serie-info li:contains(Genre) > span").first());
        } catch (Exception ignored) {
            log.debug("Failed to extract category.");
        }
        return res;
    }

    /**
     * Retrieve the status of the current serie
     *
     * @param doc the fragment html of the current serie
     * @return the scraped status
     */
    private String retrieveStatus(Document doc) {
        String res = null;
        try {
            res = ownText(doc.select("ul.serie-info li:contains(Parution) > span").first());
        } catch (Exception ignored) {
            log.debug("Failed to extract status.");
        }
        return res;
    }

    /**
     * Retrieve the origin of the current serie
     *
     * @param doc the fragment html of the current serie
     * @return the scraped origin
     */
    private String retrieveOrigin(Document doc) {
        String res = null;
        try {
            res = ownText(doc.select("ul.serie-info li:contains(Origine)").first());
        } catch (Exception ignored) {
            log.debug("Failed to extract origin.");
        }
        return res;
    }

    /**
     * Retrieve the language of the current serie
     *
     * @param doc the fragment html of the current serie
     * @return the scraped language
     */
    private String retrieveLanguage(Document doc) {
        String res = null;
        try {
            res = ownText(doc.select("ul.serie-info li:contains(Langue)").first());
        } catch (Exception ignored) {
            log.debug("Failed to extract language.");
        }
        return res;
    }

    /**
     * Retrieve the website of the current serie
     *
     * @param doc the fragment html of the current serie
     * @return the scraped origin
     */
    private String retrieveSiteUrl(Document doc) {
        String res = null;
        try {
            res = ownText(doc.select("ul.serie-info li:contains(Internet)").first());
        } catch (Exception ignored) {
            log.debug("Failed to extract website url.");
        }
        return res;
    }

    private SerieRatings retrieveRatings(Document doc) {
        var serieRatings = new SerieRatings();
        serieRatings.setCount(0);
        try {
            Elements as = doc.select("div.bandeau-menu a");
            for(Element a : as) {
                if(a.ownText().contains("Avis")) {
                    serieRatings.setUrl(a.attr("href"));
                    String serieCount = ownText(a.selectFirst("span"));
                    if(!StringUtils.isBlank(serieCount)) {
                        serieRatings.setCount(Integer.parseInt(serieCount));
                    }
                    break;
                }
            }
        } catch (Exception ignored) {
            log.debug("Failed to extract ratings.");
        }
        return serieRatings;
    }

    private String retrieveLinkedSeriesPictureUrl(Document doc) {
        String linkedSeriesPictureUrl = null;
        try {
            linkedSeriesPictureUrl = attr(doc.selectFirst("div.serie-liee img"), HTML.Attribute.SRC);
        } catch (Exception ignored) {
            log.debug("Failed to extract linkedSeriesPictureUrl.");
        }
        return linkedSeriesPictureUrl;
    }

    private List<LinkedSerie> retrieveLinkedSeries(Document doc) {
        List<LinkedSerie> linkedSeries = new ArrayList<>();
        try {
            Elements as = doc.select("div.serie-liee a");
            for(Element a : as) {
                var linkedSerie = new LinkedSerie();
                linkedSerie.setUrl(a.attr("href"));
                linkedSerie.setTitle(a.attr(HTML.Attribute.TITLE.toString()));
                linkedSerie.setExternalId(this.getIdBel(a.attr("href")));

                linkedSeries.add(linkedSerie);
            }
        } catch (Exception ignored) {
            log.debug("Failed to extract linked series.");
        }
        return linkedSeries;
    }


    private List<ToReadSerie> retrieveToReadSeries(Document doc) {
        List<ToReadSerie> toReadSeries = new ArrayList<>();
        try {
            Elements as = doc.select("div.alire div.wrapper a");
            for(Element a : as) {
                Element img = a.selectFirst("img");
                var toReadSerie = new ToReadSerie();
                toReadSerie.setUrl(a.attr("href"));
                toReadSerie.setTitle(a.attr(HTML.Attribute.TITLE.toString()));
                toReadSerie.setExternalId(this.getIdBel(a.attr("href")));
                if (img != null) {
                    toReadSerie.setCoverTitle(img.attr("alt"));
                    toReadSerie.setOriginalCoverUrl(img.attr("src"));
                    toReadSerie.setCoverFilename(getMediaFilename(toReadSerie.getOriginalCoverUrl()));
                    toReadSerie.setCoverUrl(getHashedOutputMediaUrl(toReadSerie.getOriginalCoverUrl(), httpCoverFrontThumbDirectory, toReadSerie.getExternalId()));
                    toReadSerie.setCoverPath(getHashedOutputMediaUrl(toReadSerie.getOriginalCoverUrl(), outputCoverFrontThumbDirectory, toReadSerie.getExternalId()));
                    toReadSerie.setIsCoverChecked(false);
                    toReadSerie.setCoverFileSize(0L);
                }

                toReadSeries.add(toReadSerie);
            }
        } catch (Exception ignored) {
            log.debug("Failed to extract other series to read.");
        }
        return toReadSeries;
    }

    private Integer retrieveTomeCount(Document doc) {
        Integer res = null;
        try {
            Elements lis = doc.select("ul.serie-info li");
            for(Element li : lis) {
                Element label = li.selectFirst("label");
                if(label != null && label.ownText().contains("Tome")) {
                    res = Integer.parseInt(li.ownText());
                    break;
                }
            }
        } catch (Exception ignored) {
            log.debug("Failed to extract tome count.");
        }
        return res;
    }

    /**
     * Retrieve the synopsys of the current serie
     *
     * @param doc the fragment html of the current serie
     * @return the scraped synopsys
     */
    private String retrieveSynopsys(Document doc) {
        String res = null;
        try {
            res = ownText(doc.select("div.single-content.serie > p").first());
        } catch (Exception ignored) {
            log.debug("Failed to extract synopsys.");
        }
        return res;
    }

    /**
     * Retrieve the page url of the current serie
     *
     * @param doc the fragment html of the current serie
     * @return the scraped page url
     */
    private String retrievePictureUrl(Document doc) {
        String res = null;
        try {
            res = attr(doc.select("div.serie-image > a").first(), HTML.Attribute.HREF);
        } catch (Exception ignored) {
            log.debug("Failed to extract picture url.");
        }
        return res;
    }

    /**
     * Retrieve the page thumbnail url of the current serie
     *
     * @param doc the fragment html of the current serie
     * @return the scraped page thumbnail urk
     */
    private String retrieveThumbnailUrl(Document doc) {
        String res = null;
        try {
            res = attr(doc.select("div.serie-image > a > img").first(), HTML.Attribute.SRC);
        } catch (Exception ignored) {
            log.debug("Failed to extract thumbnail picture url.");
        }
        return res;
    }

    private String retrieveCopyright(Document doc) {
        String res = null;
        try {
            res = ownText(doc.selectFirst("div.copyrightserie"));
        } catch (Exception ignored) {
            log.debug("Failed to extract copyright");
        }
        return res;
    }

    /**
     * Retrieve the number of graphic novels of the current serie
     *
     * @param doc the fragment html of the current serie
     * @return the scraped number of graphic novels
     */
    private Integer retrieveGraphicNovelCount(Document doc) {
        Integer gcCount = null;
        String res;
        try {
            Element eGraphicNovelCount = doc.select("span > i.icon-book").first();
            if(eGraphicNovelCount != null) {
                res = ownText(eGraphicNovelCount.parent());
                if(res !=null && res.contains("album")) {
                    var gcCountString =  res.split(" album")[0].trim();
                    gcCount = Integer.parseInt(gcCountString);
                }
            }
        } catch (Exception ignored) {
            log.debug("Failed to extract graphic novel count");
        }
        return gcCount;
    }

    /**
     * Retrieve the period of the current serie
     *
     * @param doc the fragment html of the current serie
     * @return the scraped period
     */
    private String retrievePeriod(Document doc) {
        String res = null;
        try {
            Element ePeriod = doc.select("span > i.icon-calendar").first();
            if(ePeriod != null) {
                res = ownText(ePeriod.parent());
            }
        } catch (Exception ignored) {
            log.debug("Failed to extract graphic novel period");
        }
        return res;
    }

    private Integer retrievePeriodFrom(String period) {
        Integer res = null;
        String[] fromTo = StringUtils.split(period, "-");
        if(fromTo.length == 2) {
            res = Integer.parseInt(fromTo[0]);
        }
        return res;
    }

    private Integer retrievePeriodTo(String period) {
        Integer res = null;
        String[] fromTo = StringUtils.split(period, "-");
        if(fromTo.length == 2) {
            res = Integer.parseInt(fromTo[1]);
        }
        return res;
    }

    /**
     * Retrieve the next serie
     *
     * @param doc the fragment html of the current serie
     * @return the scraped next serie
     */
    private Serie retrieveNextSerie(Document doc) {
        var res = new Serie();
        try {
            Element eNextSerie = doc.select("ul.nav-serie i.icon-chevron-sign-right").first();
            if(eNextSerie != null) {
                var element = eNextSerie.parent();
                res.setName(attr(element, HTML.Attribute.TITLE));
                res.setUrl(attr(element, HTML.Attribute.HREF));
                res.setId(getIdBel(res.getUrl()));
            }
        } catch (Exception ignored) {
            log.debug("Failed to extract next serie");
        }
        return res;
    }

    /**
     * Retrieve the previous serie
     *
     * @param doc the fragment html of the current serie
     * @return the scraped previous serie
     */
    private Serie retrievePreviousSerie(Document doc) {
        var res = new Serie();
        try {
            Element ePreviousSerie = doc.select("ul.nav-serie i.icon-chevron-sign-left").first();
            if(ePreviousSerie != null) {
                var element = ePreviousSerie.parent();
                res.setName(attr(element, HTML.Attribute.TITLE));
                res.setUrl(attr(element, HTML.Attribute.HREF));
                res.setId(getIdBel(res.getUrl()));
            }
        } catch (Exception ignored) {
            log.debug("Failed to extract previous serie");
        }
        return res;
    }

    private List<GraphicNovelSideListItem> retrieveGraphicNovelSideList(Document doc) {
        List<GraphicNovelSideListItem> graphicNovelSideListItems = new ArrayList<>();
        try {
            var elements = doc.select("ul.liste-albums-side li");
            for(Element element : elements) {
                var labelTag = element.selectFirst("label");
                var aTag = element.selectFirst("a");
                var spanTag = element.selectFirst("span.dl-side");
                var graphicNovelSideListItem = new GraphicNovelSideListItem();
                graphicNovelSideListItem.setExternalId(getGraphicNovelIdBEL(attr(aTag, HTML.Attribute.HREF)));
                graphicNovelSideListItem.setUrl(attr(aTag, HTML.Attribute.HREF));
                String tome = ownText(labelTag);
                if(tome != null) {
                    graphicNovelSideListItem.setTome(tome.substring(0, tome.length() - 1));
                }
                if(labelTag != null) {
                    graphicNovelSideListItem.setNumEdition(ownText(labelTag.selectFirst("span.numa")));
                }
                graphicNovelSideListItem.setTitle(attr(aTag, HTML.Attribute.TITLE));
                graphicNovelSideListItem.setPublicationDate(ownText(spanTag));
                graphicNovelSideListItems.add(graphicNovelSideListItem);
            }
        } catch (Exception ignored) {
            log.debug("Failed to extract graphic novel list");
        }
        return graphicNovelSideListItems;
    }

    private SeriePagination retrievePagination(Document doc) {
        var res = new SeriePagination();
        try {
            Element paginationTag = doc.selectFirst("div.pagination");
            if(paginationTag != null) {
                var pages = paginationTag.getElementsByTag("a");
                res.setCurrentPageNumber(ownText(paginationTag.selectFirst("span.current")));
                res.setAllGraphicNovelsInOnePageUrl(pages.getLast().attr("href"));
                res.setTotalPages(pages.get(pages.size() - 2).ownText());
                int c = Integer.parseInt(res.getCurrentPageNumber()) + 1;
                for(var i=0; i < pages.size() - 1; i++) {
                    var p = Integer.parseInt(pages.get(i).ownText());
                    if(c == p) {
                        res.setNextPageUrl(pages.get(i).attr("href"));
                        break;
                    }
                }
            } else {
                // All graphic novels are on one page
                res.setCurrentPageNumber("1");
                res.setTotalPages("1");
            }
        } catch (Exception ignored) {
            log.debug("Failed to extract serie pagination infos");
        }
        return res;
    }

    /**
     * Get the id BEL from a serie url
     * @param url the serie url
     * @return the id BEL
     */
    private String getIdBel(String url) {
        String res = null;
        String[] urlSplit = url.split("-");
        if(urlSplit.length > 2) {
            res = urlSplit[1];
        }
        return res;
    }

    private String getGraphicNovelIdBEL(String url) {
        String res = null;
        String[] urlSplit = url.split("-");
        if(urlSplit.length > 2) {
            String id = urlSplit[urlSplit.length-1];
            res = id.substring(0, id.length() - 5);
        }
        return res;
    }

}
