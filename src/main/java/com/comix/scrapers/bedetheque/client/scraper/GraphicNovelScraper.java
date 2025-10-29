package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.client.model.graphicnovel.*;
import com.comix.scrapers.bedetheque.client.model.serie.Serie;
import com.comix.scrapers.bedetheque.util.HTML;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class GraphicNovelScraper extends GenericScraper {

    private static final String SERIE = "serie";
    private static final String UNKNOWN = "<Indéterminé>";
    private static final String HTML_EXTENSION = ".html";

    // Thumbnails medias
    @Value("${application.downloads.graphic-novels.cover-front.thumbs}")
    private String outputCoverFrontThumbDirectory;

    @Value("${application.http.medias.graphic-novels.cover-front.thumbs}")
    private String httpCoverFrontThumbDirectory;

    @Value("${application.downloads.graphic-novels.cover-back.thumbs}")
    private String outputCoverBackThumbDirectory;

    @Value("${application.http.medias.graphic-novels.cover-back.thumbs}")
    private String httpCoverBackThumbDirectory;

    @Value("${application.downloads.graphic-novels.page-example.thumbs}")
    private String outputPageExampleThumbDirectory;

    @Value("${application.http.medias.graphic-novels.page-example.thumbs}")
    private String httpPageExampleThumbDirectory;

    // HD medias
    @Value("${application.downloads.graphic-novels.cover-front.hd}")
    private String outputCoverFrontHdDirectory;

    @Value("${application.http.medias.graphic-novels.cover-front.hd}")
    private String httpCoverFrontHdDirectory;

    @Value("${application.downloads.graphic-novels.cover-back.hd}")
    private String outputCoverBackHdDirectory;

    @Value("${application.http.medias.graphic-novels.cover-back.hd}")
    private String httpCoverBackHdDirectory;

    @Value("${application.downloads.graphic-novels.page-example.hd}")
    private String outputPageExampleHdDirectory;

    @Value("${application.http.medias.graphic-novels.page-example.hd}")
    private String httpPageExampleHdDirectory;

    @Value("${application.http.medias.default.unavailable}")
    private String httpDefaultMediaFilename;

    @Setter
    @Value("${application.downloads.localcache.active}")
    private boolean isLocalCacheActive;

    @Setter
    @Value("#{new Long('${application.scraping.latency}')}")
    private Long latency;

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphicNovelScraper.class);

    public static String scrapIdFromUrl(String url) {
        if(url == null) {
            return null;
        }
        String id = null;
        url = Strings.CS.remove(url, HTML_EXTENSION);
        String[] parts = StringUtils.split(url, "-");
        if (parts.length > 0 && StringUtils.isNumeric(parts[parts.length - 1])) {
            id = parts[parts.length - 1];
        }
        log.debug("Bedetheque graphic novel Id = {} for the url {}", id, url);
        return id;
    }

    public List<GraphicNovel> scrapFromSerie(String serieUrl) {
        // Load all authors starting with the letter
        Document doc = GenericScraperSingleton.getInstance().load(serieUrl, latency);
        return scrapElement(serieUrl, doc);
    }

    /**
     * Scrap a list of graphic novels
     *
     * @param scrapUrl the graphic novels url to scrap at <a href="https://www.bedetheque.com">...</a>
     * @param doc      the html document to scrap
     * @return a list of scraped graphic novels
     */
    public List<GraphicNovel> scrapElement(String scrapUrl, Document doc) {
        List<GraphicNovel> graphicNovels = new ArrayList<>();

        Elements eAlbums = doc.select("ul.liste-albums li[itemtype='https://schema.org/Book']");

        for (Element li : eAlbums) {
            graphicNovels.add(scrapElement(SERIE, scrapUrl, doc, li));
        }

        log.info("Scraped {} graphic novels from the serie url {}",
                graphicNovels.size(),
                scrapUrl);
        return graphicNovels;
    }

    /**
     * Scrap a page of graphic novels
     *
     * @param url  the graphic novels url to scrap at <a href="https://www.bedetheque.com">...</a>
     * @param page the page number to scrap (optional)
     * @return a page of scraped graphic novels
     */
    public GraphicNovelPage scrapElement(String url, int page) {
        String urlWithPage = buildURl(url, page);
        Document doc = GenericScraperSingleton.getInstance().load(urlWithPage, latency);

        GraphicNovelPage graphicNovelPage = new GraphicNovelPage();
        graphicNovelPage.setGraphicNovels(scrapFromSerie(urlWithPage));


        int graphicNovelCount = getGraphicNovelCount(doc);
        graphicNovelPage.setTotalElements(graphicNovelCount);
        if (page < 10000) {
            graphicNovelPage.setPage(page);
            graphicNovelPage.setSize(10);
            if (graphicNovelCount % 10 == 0) {
                graphicNovelPage.setTotalPages(graphicNovelCount / 10);
            } else {
                graphicNovelPage.setTotalPages(graphicNovelCount / 10 + 1);
            }
        } else {
            graphicNovelPage.setPage(1);
            graphicNovelPage.setTotalPages(1);
            graphicNovelPage.setSize(graphicNovelPage.getGraphicNovels().size());
        }
        return graphicNovelPage;
    }

    /**
     * Build the final graphic novel url to scrap at <a href="https://www.bedetheque.com">...</a>
     *
     * @param url  the graphic novels url to scrap at <a href="https://www.bedetheque.com">...</a>
     * @param page the page number to scrap (optional)
     * @return the final graphic novels url to scrap at <a href="https://www.bedetheque.com">...</a>
     */
    private String buildURl(String url, int page) {
        if (page == 1) {
            return url;
        } else if (page > 1 && page < 10000) {
            String withPage = String.format("__%d.html", page - 1);
            return url.replace(HTML_EXTENSION, withPage);
        } else {
            return url.replace(HTML_EXTENSION, "__10000.html");
        }
    }

    public GraphicNovel scrapFromSerie(String url, Document doc, Element nodeAlbum) {
        return scrapElement(SERIE, url, doc, nodeAlbum);
    }

    public List<GraphicNovel> scrapWithAllRepublications(String url) {

        Document doc = GenericScraperSingleton.getInstance().load(url, latency);

        List<GraphicNovel> graphicNovels = new ArrayList<>();

        Elements eAlbums = doc.select("ul.liste-albums > li");

        for (Element li : eAlbums) {
            graphicNovels.add(scrapElement("graphicnovel", url, doc, li));
        }

        log.info("Scraped {} graphic novels republications from the graphic novel url {}",
                graphicNovels.size(),
                url);
        return graphicNovels;
    }

    /**
     * Download all bedetheque medias on the local server
     *
     * @param graphicNovel the graphic novel
     */
    private void downloadMedias(GraphicNovel graphicNovel) {
        if (!StringUtils.isBlank(graphicNovel.getCoverThumbnailUrl())) {
            graphicNovel.setCoverThumbnailUrl(
                    downloadMedia(
                            outputCoverFrontThumbDirectory,
                            httpCoverFrontThumbDirectory,
                            graphicNovel.getCoverThumbnailUrl(),
                            httpDefaultMediaFilename,
                            graphicNovel.getExternalId()));
        }
        if (!StringUtils.isBlank(graphicNovel.getBackCoverThumbnailUrl())) {
            graphicNovel.setBackCoverThumbnailUrl(
                    downloadMedia(
                            outputCoverBackThumbDirectory,
                            httpCoverBackThumbDirectory,
                            graphicNovel.getBackCoverThumbnailUrl(),
                            httpDefaultMediaFilename,
                            graphicNovel.getExternalId()));
        }
        if (!StringUtils.isBlank(graphicNovel.getPageThumbnailUrl())) {
            graphicNovel.setPageThumbnailUrl(
                    downloadMedia(
                            outputPageExampleThumbDirectory,
                            httpPageExampleThumbDirectory,
                            graphicNovel.getPageThumbnailUrl(),
                            httpDefaultMediaFilename,
                            graphicNovel.getExternalId()));
        }

        if (!StringUtils.isBlank(graphicNovel.getCoverPictureUrl())) {
            graphicNovel.setCoverPictureUrl(
                    downloadMedia(
                            outputCoverFrontHdDirectory,
                            httpCoverFrontHdDirectory,
                            graphicNovel.getCoverPictureUrl(),
                            httpDefaultMediaFilename,
                            graphicNovel.getExternalId()));
        }
        if (!StringUtils.isBlank(graphicNovel.getBackCoverPictureUrl())) {
            graphicNovel.setBackCoverPictureUrl(
                    downloadMedia(
                            outputCoverBackHdDirectory,
                            httpCoverBackHdDirectory,
                            graphicNovel.getBackCoverPictureUrl(),
                            httpDefaultMediaFilename,
                            graphicNovel.getExternalId()));
        }
        if (!StringUtils.isBlank(graphicNovel.getPagePictureUrl())) {
            graphicNovel.setPagePictureUrl(
                    downloadMedia(
                            outputPageExampleHdDirectory,
                            httpPageExampleHdDirectory,
                            graphicNovel.getPagePictureUrl(),
                            httpDefaultMediaFilename,
                            graphicNovel.getExternalId()));
        }
    }

    private int getGraphicNovelCount(Document doc) {
        int count = 0;
        Elements bandeauMenuElements = doc.select("div.bandeau-menu > ul > li > a");
        Element albums = bandeauMenuElements.stream().filter(li -> li.ownText().contains("Albums")).findFirst().orElse(null);
        if (albums != null) {
            Element countElement = albums.selectFirst("span");
            if (countElement != null && StringUtils.isNumeric(countElement.ownText())) {
                count = Integer.parseInt(countElement.ownText());
            }
        }
        return count;
    }

    private Serie scrapSerie(String from, Element doc) {
        Serie serie = new Serie();
        Element serieElement;
        if (from.equals("graphicnovel")) {
            serieElement = doc.selectFirst("div.panier > h1 > a");
        } else {
            serieElement = doc.selectFirst("div.serie > h1 > a");
        }
        if (serieElement != null) {
            serie.setName(serieElement.ownText());
            serie.setUrl(attr(serieElement, HTML.Attribute.HREF));
            serie.setId(scrapSerieIdFromUrl(serie.getUrl()));
        }
        return serie;
    }

    private String scrapSerieIdFromUrl(String url) {
        if(url == null) {
            return null;
        }
        String id = null;
        url = Strings.CS.remove(url, HTML_EXTENSION);
        String[] parts = StringUtils.split(url, "-");
        if (parts.length >= 2 && StringUtils.isNumeric(parts[1])) {
            id = parts[1];
        }
        log.debug("Bedetheque serie Id = {} for the url {}", id, url);
        return id;
    }

    private GraphicNovel scrapElement(String from, String url, Element doc, Element gcElement) {
        // Serie
        Serie serie = scrapSerie(from, doc);

        // Album main
        Element title;
        if (from.equals("graphicnovel")) {
            title = gcElement.selectFirst("h3.titre"); //NOSONAR
        } else {
            title = gcElement.selectFirst("h3 a.titre > span[itemprop='name']");
        }

        var infos = gcElement.selectFirst("ul.infos");
        var autres = gcElement.selectFirst("div.autres");
        var side = gcElement.selectFirst("div.album-side");
        var ratings = gcElement.selectFirst("div.eval");

        String tome = getTome(from, title);
        Integer tomeNum = null;
        try {
            tomeNum = Integer.parseInt(tome);
        } catch (Exception ex) {
            LOGGER.debug("Failed to convert tome in integer");
        }

        var graphicNovel = new GraphicNovel();
        graphicNovel.setSerie(serie);
        graphicNovel.setTome(tome);
        graphicNovel.setTomeNum(tomeNum);
        graphicNovel.setNumEdition(getNumEdition(title));
        graphicNovel.setTitle(getTitle(from, title));
        graphicNovel.setExternalId(getExternalId(infos));
        graphicNovel.setRatings(getRatings(ratings));
        graphicNovel.setAuthors(getAuthors(infos));
        graphicNovel.setPublicationDate(getPublicationDate(infos));
        graphicNovel.setReleaseDate(getReleaseDate(infos));
        graphicNovel.setPublisher(getPublisher(infos));
        graphicNovel.setCollection(getCollection(infos));
        graphicNovel.setCollectionUrl(getCollectionUrl(infos));
        graphicNovel.setCycle(getCycle(infos));
        graphicNovel.setIsbn(getIsbn(infos));
        graphicNovel.setTotalPages(getTotalPages(infos));
        graphicNovel.setFormat(getFormat(infos));
        graphicNovel.setIsOriginalPublication(isOriginalPublication(infos));
        graphicNovel.setIsIntegrale(isIntegrale(infos));
        graphicNovel.setIsBroche(isBroche(infos));
        graphicNovel.setInfoEdition(getInfoEdition(autres));
        graphicNovel.setReeditionUrl(getReeditionUrl(autres));
        graphicNovel.setReeditionCount(getReeditionCount(autres));
        graphicNovel.setExternalIdOriginalPublication(getExternalIdOriginalPublication(doc));
        graphicNovel.setCoverPictureUrl(getCoverPictureUrl(side));
        graphicNovel.setCoverThumbnailUrl(getCoverThumbnailUrl(side));
        graphicNovel.setCopyright(getCopyright(side));
        graphicNovel.setBackCoverPictureUrl(getBackCoverPictureUrl(side));
        graphicNovel.setBackCoverThumbnailUrl(getBackCoverThumbnailUrl(side));
        graphicNovel.setPagePictureUrl(getPagePictureUrl(side));
        graphicNovel.setPageThumbnailUrl(getPageThumbnailUrl(side));
        graphicNovel.setScrapUrl(url);

        // Download all thumbs in the local server
        if (isLocalCacheActive) {
            downloadMedias(graphicNovel);
        }
        return graphicNovel;
    }

    private String getTome(String from, Element e) {
        String tome = null;
        try {
            String[] blocTome = null;
            Element eSerie = e.selectFirst("span[itemprop=name]");
            Element eOther = e.selectFirst("h3.titre");
            if (from.equals(SERIE) && eSerie != null) {
                blocTome = eSerie.html().split("(<span class=\"numa\">)");
            } else if (eOther != null) {
                blocTome = eOther.html().split("(<span class=\"numa\">)");
            }

            if (blocTome != null && blocTome.length >= 1) {
                tome = blocTome[0].trim();
            }
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap tome");
        }
        return tome;
    }

    private String getNumEdition(Element e) {
        String numa = null;
        try {
            numa = ownText(e.selectFirst("span.numa"));
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap numa");
        }
        return numa;
    }

    private String getTitle(String from, Element e) {
        String title = null;
        try {
            String[] tb = null;
            Element eSerie = e.selectFirst("span[itemprop=name]");
            Element eOther = e.selectFirst("h3.titre");
            if (from.equals(SERIE) && eSerie != null) {
                tb = eSerie.html().split("(</span>)");
            } else if (eOther != null) {
                tb = eOther.html().split("(</span>)");
            }
            if (tb != null && tb.length >= 1 && tb[1] != null) {
                title = tb[1].trim();
                // Delete first .
                if (title.startsWith(". ")) {
                    title = title.substring(2).trim();
                }
            }

        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap title");
        }
        return title;
    }

    private Ratings getRatings(Element e) {
        Ratings ratings = null;
        try {
            String width = attr(e.selectFirst("ul.unit-rating li"), HTML.Attribute.STYLE);
            String count = ownText(e.selectFirst("p.message")).split("Note:")[1];
            String rating = ownText(e.selectFirst("p.message > strong"));
            ratings = new Ratings();
            ratings.setRating(rating);
            ratings.setCount(count);
            ratings.setWidth(width);
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap ratings");
        }
        return ratings;
    }

    private String getExternalId(Element e) {
        String externalId = null;
        try {
            Element eId = e.selectFirst("li label:containsOwn(Identifiant)");
            if (eId != null) {
                externalId = ownText(eId.parent());
            }
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap external id");
        }
        return externalId;
    }

    private List<AuthorRole> getAuthors(Element e) {
        if (e == null) {
            return Collections.emptyList();
        }
        List<AuthorRole> authors = new ArrayList<>();

        List<Element> infos = e.select("li");
        var key = "";
        var hasLabel = false;
        for (Element li : infos) {
            // Identify the current key
            var label = ownText(li.selectFirst("label"));
            if (!StringUtils.isBlank(label) && label.contains(":")) {
                label = StringUtils.split(label, ":")[0].trim();
                hasLabel = true;
            } else {
                hasLabel = false;
            }
            if (!StringUtils.isBlank(label) && label.compareTo("") > 0) {
                key = label;
            }

            // Authors roles :
            //  -> Scénario, Dessin, Couleurs, Storyboard, Encrage, Lettrage, Couverture,
            //     Autres, Décors, Traduction, Préface, Adapté de, Design
            if (key.equals("Scénario") || key.equals("Dessin") || key.equals("Couleurs") || key.equals("Storyboard") ||
                    key.equals("Encrage") || key.equals("Lettrage") || key.equals("Couverture") || key.equals("Autres") ||
                    key.equals("Décors") || key.equals("Traduction") || key.equals("Préface") || key.equals("Adapté de") ||
                    key.equals("Design")) {

                authors.add(getAuthorRole(li, key, hasLabel));
            }
        }
        return authors;
    }

    private AuthorRole getAuthorRole(Element li, String key, boolean hasLabel) {
        AuthorRole authorRole = new AuthorRole();
        Element span = li.selectFirst("a > span");
        var name = UNKNOWN;
        if (span == null) {
            Element a = li.selectFirst("a");
            if (a != null) {
                name = ownText(li.selectFirst("a"));
            }
        } else {
            name = span.ownText();
        }
        String url = null;
        Element a = li.selectFirst("a");
        if (a != null) {
            url = a.attr("href");
        }
        String displayedRole = key;
        if (!hasLabel) {
            displayedRole = "";
        }
        try {
            var externalId = "";
            if (url != null) {
                externalId = url.split("-")[1];
            }

            authorRole.setRole(key);
            authorRole.setDisplayedRole(displayedRole);
            authorRole.setExternalId(externalId);
            authorRole.setName(name);
            authorRole.setAuthorUrl(url);
        } catch (Exception ex) {
            authorRole.setRole(UNKNOWN);
            authorRole.setExternalId("1");
            authorRole.setName(UNKNOWN);
            authorRole.setAuthorUrl("https://www.bedetheque.com/auteur-1-BD-Indetermine.html");
        }
        return authorRole;
    }

    private String getPublicationDate(Element e) {
        if (e == null) {
            return null;
        }
        String publicationDate = null;
        Element ePubDate = e.selectFirst("li label:containsOwn(Dépot légal)");
        if (ePubDate != null) {
            String value = ownText(ePubDate.parent());
            if (!StringUtils.isBlank(value) && value.contains("Parution")) {
                value = value.replace('(', ' ');
                value = value.replace(')', ' ');
                String[] tbParution = value.split("Parution le");
                publicationDate = tbParution[0].trim();
            } else {
                publicationDate = value;
            }
        }

        return publicationDate;
    }

    private String getReleaseDate(Element e) {
        String releaseDate = null;
        try {
            Element eReleaseDate = e.selectFirst("li label:containsOwn(Dépot légal)");
            if (eReleaseDate != null) {
                String value = ownText(eReleaseDate.nextElementSibling());
                if (!StringUtils.isBlank(value) && value.contains("Parution")) {
                    value = value.replace('(', ' ');
                    value = value.replace(')', ' ');
                    String[] tbParution = value.split("Parution le");

                    if (tbParution.length > 1) {
                        releaseDate = tbParution[1].trim();
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap release date");
        }
        return releaseDate;
    }

    private String getPublisher(Element e) {
        String publisher = null;
        try {
            publisher = getInfoPropertyValue(e, "Editeur");
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap publisher");
        }
        return publisher;
    }

    private String getCollection(Element e) {
        String collection = null;
        try {
            Element eCollection = e.selectFirst("li label:containsOwn(Collection)");
            if (eCollection != null && eCollection.parent() != null) {
                collection = ownText(eCollection.parent().selectFirst("a"));
            }
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap collection");
        }
        return collection;
    }

    private String getCollectionUrl(Element e) {
        String collectionUrl = null;
        try {
            Element eCollectionUrl = e.selectFirst("li label:containsOwn(Collection)");
            if (eCollectionUrl != null && eCollectionUrl.parent() != null) {
                collectionUrl = attr(eCollectionUrl.parent().selectFirst("a"), HTML.Attribute.HREF);
            }
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap collectionUrl");
        }
        return collectionUrl;
    }

    private String getCycle(Element e) {
        String cycle = null;
        try {
            cycle = getInfoPropertyValue(e, "Cycle");
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap cycle");
        }
        return cycle;
    }

    private String getIsbn(Element e) {
        String isbn = null;
        try {
            isbn = getInfoPropertyValue(e, "ISBN");
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap isbn");
        }
        return isbn;
    }

    private Integer getTotalPages(Element e) {
        Integer pages = null;
        try {
            pages = Integer.parseInt(getInfoPropertyValue(e, "Planches"));
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap page count");
        }
        return pages;
    }

    private String getFormat(Element e) {
        String format = null;
        try {
            Element eFormat = e.selectFirst("li label:containsOwn(Format)");
            if (eFormat != null && eFormat.parent() != null) {
                format = eFormat.parent().ownText();
            }
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap format");
        }
        return format;
    }

    private Boolean isOriginalPublication(Element e) {
        var isOriginalPublication = false;
        try {
            Element eIsOriginalPublication = e.selectFirst("li label:containsOwn(Autres infos)"); //NOSONAR
            if (eIsOriginalPublication != null && eIsOriginalPublication.parent() != null) {
                Element icon = eIsOriginalPublication.parent().selectFirst("i.icon-star");
                if (icon != null) {
                    isOriginalPublication = true;
                }
            }
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap isOriginalPublication");
        }
        return isOriginalPublication;
    }

    private Boolean isIntegrale(Element e) {
        var isIntegrale = false;
        try {
            Element eIsIntegrale = e.selectFirst("li label:containsOwn(Autres infos)");
            if (eIsIntegrale != null && eIsIntegrale.parent() != null) {
                Element icon = eIsIntegrale.parent().selectFirst("i.icon-pause");
                if (icon != null) {
                    isIntegrale = true;
                }
            }
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap isIntegrale");
        }
        return isIntegrale;
    }

    private Boolean isBroche(Element e) {
        var isBroche = false;
        try {
            Element eIsBroche = e.selectFirst("li label:containsOwn(Autres infos)");
            if (eIsBroche != null && eIsBroche.parent() != null) {
                Element icon = eIsBroche.parent().selectFirst("i.icon-tag");
                if (icon != null) {
                    isBroche = true;
                }
            }
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap isBroche");
        }
        return isBroche;
    }

    private String getInfoEdition(Element e) {
        String infos = null;
        try {
            infos = ownText(e.selectFirst("p"));
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap infos edition");
        }
        return infos;
    }

    private String getReeditionUrl(Element e) {
        String reeditionUrl = null;
        try {
            Element eReeditionUrl = e.selectFirst("em:containsOwn(Rééditions)");
            if (eReeditionUrl != null && eReeditionUrl.parent() != null) {
                reeditionUrl = eReeditionUrl.parent().attr("href");
            }
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap reeditionUrl");
        }
        return reeditionUrl;
    }

    private String getReeditionCount(Element e) {
        String reeditionCount = null;
        try {
            Element eReeditionCount = e.selectFirst("em:containsOwn(Rééditions)");
            if (eReeditionCount != null && eReeditionCount.parent() != null) {
                reeditionCount = ownText(eReeditionCount.parent().selectFirst("strong"));
            }
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap reeditionCount");
        }
        return reeditionCount;
    }

    private String getExternalIdOriginalPublication(Element e) {
        String externalIdOriginalPublication = null;
        try {
            Element eExternalIdOriginalPublication = e.selectFirst("ul.infos-albums li label:containsOwn(Identifiant)");
            if (eExternalIdOriginalPublication != null && eExternalIdOriginalPublication.parent() != null) {
                externalIdOriginalPublication = eExternalIdOriginalPublication.parent().ownText();
            }
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap externalIdOriginalPublication");
        }
        return externalIdOriginalPublication;
    }

    private String getCoverPictureUrl(Element e) {
        String coverPictureUrl = null;
        try {
            coverPictureUrl = attr(e.selectFirst("div.sous-couv a.browse-couvertures"), HTML.Attribute.HREF);
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap coverPictureUrl");
        }
        return coverPictureUrl;
    }

    private String getCoverThumbnailUrl(Element e) {
        String coverThumbnailUrl = null;
        try {
            coverThumbnailUrl = attr(e.selectFirst("div.couv img"), HTML.Attribute.SRC);
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap coverThumbnailUrl");
        }
        return coverThumbnailUrl;
    }

    private String getCopyright(Element e) {
        String copyright = null;
        try {
            copyright = ownText(e.selectFirst("div.couv span"));
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap copyright");
        }
        return copyright;
    }

    private String getBackCoverPictureUrl(Element e) {
        String backCoverPictureUrl = null;
        try {
            backCoverPictureUrl = attr(e.selectFirst("div.sous-couv a.browse-versos"), HTML.Attribute.HREF);
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap backCoverPictureUrl");
        }
        return backCoverPictureUrl;
    }

    private String getBackCoverThumbnailUrl(Element e) {
        String backCoverThumbnailUrl = null;
        try {
            backCoverThumbnailUrl = attr(e.selectFirst("div.sous-couv a.browse-versos img"), HTML.Attribute.SRC);
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap backCoverThumbnailUrl");
        }
        return backCoverThumbnailUrl;
    }

    private String getPagePictureUrl(Element e) {
        String pagePictureUrl = null;
        try {
            pagePictureUrl = attr(e.selectFirst("div.sous-couv a.browse-planches"), HTML.Attribute.HREF);
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap pagePictureUrl");
        }
        return pagePictureUrl;
    }

    private String getPageThumbnailUrl(Element e) {
        String pageThumbnailUrl = null;
        try {
            pageThumbnailUrl = attr(e.selectFirst("div.sous-couv a.browse-planches img"), HTML.Attribute.SRC);
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap pageThumbnailUrl");
        }
        return pageThumbnailUrl;
    }

    private String getInfoPropertyValue(Element e, String key) {
        String value = null;
        var selector = "li label:containsOwn(%s)".formatted(key);
        try {
            Element eInfoPropertyValue = e.selectFirst(selector);
            if (eInfoPropertyValue != null) {
                var span = eInfoPropertyValue.nextElementSibling();
                if (span != null) {
                    value = span.ownText();
                } else {
                    value = ownText(eInfoPropertyValue.parent());
                }
            }
        } catch (Exception ex) {
            LOGGER.debug("Failed to scrap property : {}", key);
        }
        return value;
    }

}
