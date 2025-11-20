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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class GraphicNovelScraper extends GenericScraper {

    private static final String SERIE = "serie";
    private static final String GRAPHIC_NOVEL = "graphicnovel";
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

    private static final Set<String> AUTHOR_ROLES = Set.of(
            "Scénario", "Dessin", "Couleurs", "Storyboard", "Encrage", "Lettrage",
            "Couverture", "Autres", "Décors", "Traduction", "Préface", "Adapté de", "Design"
    );

    /**
     * Extracts the Bedetheque ID from a graphic novel URL.
     * @param url The URL of the graphic novel.
     * @return The extracted ID, or null if not found.
     */
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
            graphicNovels.add(scrapElement(GRAPHIC_NOVEL, url, doc, li));
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
        graphicNovel.setCoverThumbnailUrl(downloadAndSetMedia(graphicNovel.getCoverThumbnailUrl(), outputCoverFrontThumbDirectory, httpCoverFrontThumbDirectory, graphicNovel.getExternalId()));
        graphicNovel.setBackCoverThumbnailUrl(downloadAndSetMedia(graphicNovel.getBackCoverThumbnailUrl(), outputCoverBackThumbDirectory, httpCoverBackThumbDirectory, graphicNovel.getExternalId()));
        graphicNovel.setPageThumbnailUrl(downloadAndSetMedia(graphicNovel.getPageThumbnailUrl(), outputPageExampleThumbDirectory, httpPageExampleThumbDirectory, graphicNovel.getExternalId()));

        graphicNovel.setCoverPictureUrl(downloadAndSetMedia(graphicNovel.getCoverPictureUrl(), outputCoverFrontHdDirectory, httpCoverFrontHdDirectory, graphicNovel.getExternalId()));
        graphicNovel.setBackCoverPictureUrl(downloadAndSetMedia(graphicNovel.getBackCoverPictureUrl(), outputCoverBackHdDirectory, httpCoverBackHdDirectory, graphicNovel.getExternalId()));
        graphicNovel.setPagePictureUrl(downloadAndSetMedia(graphicNovel.getPagePictureUrl(), outputPageExampleHdDirectory, httpPageExampleHdDirectory, graphicNovel.getExternalId()));
    }

    private String downloadAndSetMedia(String url, String outputDir, String httpDir, String externalId) {
        if (StringUtils.isBlank(url)) {
            return url;
        }
        return downloadMedia(outputDir, httpDir, url, httpDefaultMediaFilename, externalId);
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
        if (GRAPHIC_NOVEL.equals(from)) {
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
        if (GRAPHIC_NOVEL.equals(from)) {
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
            log.debug("Failed to convert tome '{}' to integer", tome);
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
        graphicNovel.setTotalPages(getIntegerInfoProperty(infos, "Planches"));
        graphicNovel.setFormat(getFormat(infos));
        graphicNovel.setIsOriginalPublication(hasIcon(infos, "i.icon-star"));
        graphicNovel.setIsIntegrale(hasIcon(infos, "i.icon-pause"));
        graphicNovel.setIsBroche(hasIcon(infos, "i.icon-tag"));
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
            log.debug("Failed to scrap tome", ex);
        }
        return tome;
    }

    private String getNumEdition(Element e) {
        String numa = null;
        try {
            numa = ownText(e.selectFirst("span.numa"));
        } catch (Exception ex) {
            log.debug("Failed to scrap numa", ex);
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
            log.debug("Failed to scrap title", ex);
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
            log.debug("Failed to scrap ratings", ex);
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
            log.debug("Failed to scrap external id", ex);
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
            if (AUTHOR_ROLES.contains(key)) {
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
            log.debug("Failed to scrap release date", ex);
        }
        return releaseDate;
    }

    private String getPublisher(Element e) {
        try {
            return getInfoPropertyValue(e, "Editeur");
        } catch (Exception ex) {
            log.debug("Failed to scrap publisher", ex);
            return null;
        }
    }

    private String getCollection(Element e) {
        String collection = null;
        try {
            Element eCollection = e.selectFirst("li label:containsOwn(Collection)");
            if (eCollection != null && eCollection.parent() != null) {
                collection = ownText(eCollection.parent().selectFirst("a"));
            }
        } catch (Exception ex) {
            log.debug("Failed to scrap collection", ex);
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
            log.debug("Failed to scrap collectionUrl", ex);
        }
        return collectionUrl;
    }

    private String getCycle(Element e) {
        try {
            return getInfoPropertyValue(e, "Cycle");
        } catch (Exception ex) {
            log.debug("Failed to scrap cycle", ex);
            return null;
        }
    }

    private String getIsbn(Element e) {
        try {
            return getInfoPropertyValue(e, "ISBN");
        } catch (Exception ex) {
            log.debug("Failed to scrap isbn", ex);
            return null;
        }
    }

    private Integer getIntegerInfoProperty(Element e, String key) {
        Integer pages = null;
        try {
            String value = getInfoPropertyValue(e, key);
            if (StringUtils.isNumeric(value)) {
                pages = Integer.parseInt(value);
            }
        } catch (Exception ex) {
            log.debug("Failed to scrap integer property {}", key, ex);
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
            log.debug("Failed to scrap format", ex);
        }
        return format;
    }

    private boolean hasIcon(Element e, String iconCssSelector) {
        if (e == null) {
            return false;
        }
        try {
            Element parent = e.selectFirst("li label:containsOwn(Autres infos)");
            return parent != null && parent.parent() != null && parent.parent().selectFirst(iconCssSelector) != null;
        } catch (Exception ex) {
            log.debug("Failed to check for icon '{}'", iconCssSelector, ex);
            return false;
        }
    }

    private String getInfoEdition(Element e) {
        return scrapSafe(e, "p", this::ownText, "infos edition");
    }

    private String getReeditionUrl(Element e) {
        String reeditionUrl = null;
        try {
            Element eReeditionUrl = e.selectFirst("em:containsOwn(Rééditions)");
            if (eReeditionUrl != null && eReeditionUrl.parent() != null) {
                reeditionUrl = eReeditionUrl.parent().attr("href");
            }
        } catch (Exception ex) {
            log.debug("Failed to scrap reeditionUrl", ex);
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
            log.debug("Failed to scrap reeditionCount", ex);
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
            log.debug("Failed to scrap externalIdOriginalPublication", ex);
        }
        return externalIdOriginalPublication;
    }

    private String getCoverPictureUrl(Element e) {
        return scrapAttribute(e, "div.sous-couv a.browse-couvertures", HTML.Attribute.HREF, "coverPictureUrl");
    }

    private String getCoverThumbnailUrl(Element e) {
        return scrapAttribute(e, "div.couv img", HTML.Attribute.SRC, "coverThumbnailUrl");
    }

    private String getCopyright(Element e) {
        return scrapSafe(e, "div.couv span", this::ownText, "copyright");
    }

    private String getBackCoverPictureUrl(Element e) {
        return scrapAttribute(e, "div.sous-couv a.browse-versos", HTML.Attribute.HREF, "backCoverPictureUrl");
    }

    private String getBackCoverThumbnailUrl(Element e) {
        return scrapAttribute(e, "div.sous-couv a.browse-versos img", HTML.Attribute.SRC, "backCoverThumbnailUrl");
    }

    private String getPagePictureUrl(Element e) {
        return scrapAttribute(e, "div.sous-couv a.browse-planches", HTML.Attribute.HREF, "pagePictureUrl");
    }

    private String getPageThumbnailUrl(Element e) {
        return scrapAttribute(e, "div.sous-couv a.browse-planches img", HTML.Attribute.SRC, "pageThumbnailUrl");
    }

    private String scrapAttribute(Element e, String selector, HTML.Attribute attribute, String propertyName) {
        return scrapSafe(e, selector, el -> attr(el, attribute), propertyName);
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
            log.debug("Failed to scrap property : {}", key, ex);
        }
        return value;
    }

    private <T> T scrapSafe(Element e, String selector, java.util.function.Function<Element, T> extractor, String propertyName) {
        if (e == null) {
            return null;
        }
        try {
            Element selectedElement = e.selectFirst(selector);
            return (selectedElement != null) ? extractor.apply(selectedElement) : null;
        } catch (Exception ex) {
            log.debug("Failed to scrap {}: {}", propertyName, ex.getMessage());
            return null;
        }
    }
}
