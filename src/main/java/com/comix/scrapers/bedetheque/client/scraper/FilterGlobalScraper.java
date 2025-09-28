package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.client.model.filter.*;
import com.comix.scrapers.bedetheque.util.HTML;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Slf4j
@Component
public class FilterGlobalScraper extends Scraper {

    private static final String CSS_SELECTOR_FLAG_URL = "span.ico > img";

    @Value("${bedetheque.url.search.global}")
    private String bedethequeGlobalSearchUrl;

    @Value("${application.downloads.localcache.active}")
    private boolean isLocalCacheActive;

    @Value("#{new Long('${application.scraping.latency}')}")
    private Long latency;

    /**
     * Find series, graphic novels, authors, news, chronicles, previews by Global search filter
     * @param filter the filter
     * @return the filtered results
     */
    public GlobalFilteredObject filter(String filter) {

        var globalFilteredObject = new GlobalFilteredObject();
        globalFilteredObject.setFilter(filter);

        // Get the page
        Document doc = load(filter);
        // Extract results
        List<Element> results = doc.select("div.search-line");
        for (Element e : results) {
            String title = ownText(e.selectFirst("h3"));
            if(!StringUtils.isBlank(title)) {
                filterTitle(e, title, globalFilteredObject);
            }
        }
        log.info("With filter {}, scraped {} chronicles, {} news, {} previews, {} authors, {} series, {} albums",
                filter,
                globalFilteredObject.getFilteredChronicles().size(),
                globalFilteredObject.getFilteredNews().size(),
                globalFilteredObject.getFilteredPreviews().size(),
                globalFilteredObject.getFilteredAuthors().size(),
                globalFilteredObject.getFilteredSeries().size(),
                globalFilteredObject.getFilteredGraphicNovels().size());
        return globalFilteredObject;
    }

    private void filterTitle(Element e, String title, GlobalFilteredObject globalFilteredObject) {
        // Get chronicle
        if(title.contains("chronique")) {
            getChronicles(e, globalFilteredObject);
        }
        // Get news
        if(title.contains("news")){
            getNews(e, globalFilteredObject);
        }
        // Get previews
        if(title.contains("preview")) {
            getPreviews(e, globalFilteredObject);
        }
        // Get authors
        if(title.contains("auteur")){
            getAuthors(e, globalFilteredObject);
        }
        // Get series
        if(title.contains("série") && !title.contains("mot clé")) {
            getSeries(e, globalFilteredObject);
        }
        // Get associate series
        if(title.contains("série") && title.contains("mot clé")) {
            getAssociateSeries(e, globalFilteredObject);
        }
        // Get albums
        if(title.contains("album")) {
            getGraphicNovels(e, globalFilteredObject);
        }
    }

    /**
     * Load the content of a bedetheque global search
     * @param filter the filter to apply
     * @return the content of the result page
     */
    private Document load(String filter) {
        Map<String, String> data = new HashMap<>();
        data.put("RechWhere", "1");
        data.put("RechTexte", filter);
        return GenericScraperSingleton.getInstance().load(bedethequeGlobalSearchUrl, data, latency);
    }

    /**
     * Get Chronicles
     * @param title the title tag
     */
    private void getChronicles(Element title, GlobalFilteredObject globalFilteredObject) {
        List<FilteredChronicle> filteredChronicles = new ArrayList<>();
        // First sibling => div.clear
        // Second sibling => ul or p if nothing or too many elements found
        Element nextElementSibling = title.nextElementSibling();
        var list = (nextElementSibling != null) ? nextElementSibling.nextElementSibling() : null;
        // Add all chronicles
        if(list != null && list.is("ul")) {
            List<Element> liEls = list.select("li");
            for(Element e : liEls) {
                // Get rating
                var ratingElement = e.selectFirst("span.count > img");
                String ratingLabel = (ratingElement != null ? ratingElement.attr("title").trim() : "");
                String ratingUrl = (ratingElement != null ? ratingElement.attr("src") : "");
                // Add a chronicle in the result list
                var filteredChronicle = new FilteredChronicle();
                filteredChronicle.setName(text(e.selectFirst(HTML.Tag.A + " > " + HTML.Tag.SPAN)));
                filteredChronicle.setRatingLabel(ratingLabel);
                filteredChronicle.setRatingUrl(ratingUrl);
                filteredChronicle.setUrl(attr(e.selectFirst("a"), HTML.Attribute.HREF));
                filteredChronicles.add(filteredChronicle);
            }
            globalFilteredObject.setFilteredChronicles(filteredChronicles);
        }
        // Too much results
        if(list != null && list.is("p")) {
            globalFilteredObject.setFilteredChroniclesMessage(list.text().trim());
        }
    }

    /**
     * Get news
     * @param title the title tag
     */
    private void getNews(Element title, GlobalFilteredObject globalFilteredObject) {
        List<FilteredNews> filteredNews = new ArrayList<>();
        // First sibling => div.clear
        // Second sibling => ul or p if nothing or too many elements found
        Element nextElementSibling = title.nextElementSibling();
        var list = (nextElementSibling != null) ? nextElementSibling.nextElementSibling() : null;
        // Add all news
        if(list != null && list.is("ul")) {
            List<Element> liEls = list.select("li");
            for(Element e : liEls) {
                // Get origin
                var originElement = e.selectFirst(HTML.Tag.SPAN + JSOUP_TAG_COUNT);
                String origin = (originElement != null ? originElement.text().trim() : "");
                // Add news in the result list
                FilteredNews filteredNew = new FilteredNews();
                filteredNew.setTitle(text(e.selectFirst(HTML.Tag.A + " > " + HTML.Tag.SPAN)));
                filteredNew.setOrigin(origin);
                filteredNew.setUrl(attr(e.selectFirst("a"), HTML.Attribute.HREF));
                filteredNews.add(filteredNew);
            }
            globalFilteredObject.setFilteredNews(filteredNews);
        }
        // Too much results
        if(list != null && list.is("p")) {
            globalFilteredObject.setFilteredNewsMessage(list.text().trim());
        }
    }

    /**
     * Get previews
     * @param title the title tag
     */
    private void getPreviews(Element title, GlobalFilteredObject globalFilteredObject) {
        List<FilteredPreview> filteredPreviews = new ArrayList<>();
        // First sibling => div.clear
        // Second sibling => ul or p if nothing or too many elements found
        Element nextElementSibling = title.nextElementSibling();
        var list = (nextElementSibling != null) ? nextElementSibling.nextElementSibling() : null;
        // Add all previews
        if(list != null && list.is("ul")) {
            List<Element> liEls = list.select("li");
            for(Element e : liEls) {
                // Get origin
                var pagesCountElement = e.selectFirst(HTML.Tag.SPAN + JSOUP_TAG_COUNT);
                String pagesCount = (pagesCountElement != null ? pagesCountElement.text().trim() : "");
                // Add a preview in the result list
                var filteredPreview = new FilteredPreview();
                filteredPreview.setTitle(text(e.selectFirst(HTML.Tag.A + " > " + HTML.Tag.SPAN)));
                filteredPreview.setPagesCount(pagesCount);
                filteredPreview.setUrl(attr(e.selectFirst("a"), HTML.Attribute.HREF));
                filteredPreviews.add(filteredPreview);
            }
            globalFilteredObject.setFilteredPreviews(filteredPreviews);
        }
        // Too much results
        if(list != null && list.is("p")) {
            globalFilteredObject.setFilteredPreviewsMessage(list.text().trim());
        }
    }

    /**
     * Extract all authors
     * @param title the title tag
     */
    private void getAuthors(Element title, GlobalFilteredObject globalFilteredObject) {
        List<FilteredAuthor> filteredAuthors = new ArrayList<>();
        // First sibling => div.clear
        // Second sibling => ul or p if nothing or too many elements found
        Element nextElementSibling = title.nextElementSibling();
        var list = (nextElementSibling != null) ? nextElementSibling.nextElementSibling() : null;
        // Add all authors
        if(list != null && list.is("ul")) {
            List<Element> liEls = list.select("li");
            for(Element e : liEls) {
                // Get nationality
                var nationalityElement = e.selectFirst(HTML.Tag.SPAN + JSOUP_TAG_COUNT);
                String nationality = (nationalityElement != null ? nationalityElement.text().trim() : "");
                // Add an author in the result list
                var filteredAuthor = new FilteredAuthor();
                filteredAuthor.setName(text(e.selectFirst(HTML.Tag.A + " > " + HTML.Tag.SPAN)));
                filteredAuthor.setNationality(nationality);
                filteredAuthor.setUrl(attr(e.selectFirst("a"), HTML.Attribute.HREF));
                filteredAuthors.add(filteredAuthor);
            }
            globalFilteredObject.setFilteredAuthors(filteredAuthors);
        }
        // Too much results
        if(list != null && list.is("p")) {
            globalFilteredObject.setFilteredAuthorsMessage(list.text().trim());
        }
    }

    /**
     * Get series
     * @param title the title tag
     */
    private void getSeries(Element title, GlobalFilteredObject globalFilteredObject) {
        List<FilteredSerie> filteredSeries = new ArrayList<>();
        // First sibling => div.clear
        // Second sibling => ul or p if nothing or too many elements found
        Element nextElementSibling = title.nextElementSibling();
        var list = (nextElementSibling != null) ? nextElementSibling.nextElementSibling() : null;
        // Add all series
        if(list != null && list.is("ul")) {
            List<Element> liElements = list.select("li");
            for(Element e : liElements) {
                filteredSeries.add(getFilteredSerie(e));
            }
            globalFilteredObject.setFilteredSeries(filteredSeries);
        }
        // Too much results
        if(list != null && list.is("p")) {
            globalFilteredObject.setFilteredSeriesMessage(list.text().trim());
        }
    }

    private FilteredSerie getFilteredSerie(Element e) {
        // Get category
        var categoryElement = e.selectFirst(HTML.Tag.SPAN + JSOUP_TAG_COUNT);
        String category = (categoryElement != null ? categoryElement.text().trim() : "");
        // Add a serie in the result list
        var filteredSerie = new FilteredSerie();
        filteredSerie.setFlagUrl(attr(e.selectFirst(CSS_SELECTOR_FLAG_URL), HTML.Attribute.SRC));
        filteredSerie.setTitle(text(e.selectFirst(HTML.Tag.A + " > " + HTML.Tag.SPAN)));
        filteredSerie.setCategory(category);
        filteredSerie.setUrl(attr(e.selectFirst("a"), HTML.Attribute.HREF));
        return filteredSerie;
    }

    /**
     * Get associate series
     * @param title the title tag
     */
    private void getAssociateSeries(Element title, GlobalFilteredObject globalFilteredObject) {
        List<FilteredSerie> filteredSeries = new ArrayList<>();
        // First sibling => div.clear
        // Second sibling => ul or p if nothing or too many elements found
        Element nextElementSibling = title.nextElementSibling();
        var list = (nextElementSibling != null) ? nextElementSibling.nextElementSibling() : null;
        // Add all series
        if(list != null && list.is("ul")) {
            List<Element> liElements = list.select("li");
            for(Element e : liElements) {
                filteredSeries.add(getFilteredSerie(e));
            }
            globalFilteredObject.setFilteredAssociateSeries(filteredSeries);
        }
        // Too much results
        if(list != null && list.is("p")) {
            globalFilteredObject.setFilteredAssociateSeriesMessage(list.text().trim());
        }
    }

    /**
     * Get graphic novels
     * @param title the title tag
     */
    private void getGraphicNovels(Element title, GlobalFilteredObject globalFilteredObject) {
        List<FilteredGraphicNovel> filteredGraphicNovels = new ArrayList<>();
        // First sibling => div.clear
        // Second sibling => ul or p if nothing or too many elements found
        Element nextElementSibling = title.nextElementSibling();
        var list = (nextElementSibling != null) ? nextElementSibling.nextElementSibling() : null;
        // Add all graphic novels
        if(list != null && list.is("ul")) {
            List<Element> liElements = list.select("li");
            for(Element e : liElements) {
                // Get publication date
                var publicationDateElement = e.selectFirst(HTML.Tag.SPAN + JSOUP_TAG_COUNT);
                String publicationDate = (publicationDateElement != null ? publicationDateElement.text().trim() : "");
                // Add a graphic novel in the result list
                var filteredGraphicNovel = new FilteredGraphicNovel();
                filteredGraphicNovel.setFlagUrl(attr(e.selectFirst(CSS_SELECTOR_FLAG_URL), HTML.Attribute.SRC));
                filteredGraphicNovel.setTitle(text(e.selectFirst(HTML.Tag.A + " > " + HTML.Tag.SPAN)));
                filteredGraphicNovel.setPublicationDate(publicationDate);
                filteredGraphicNovel.setUrl(attr(e.selectFirst("a"), HTML.Attribute.HREF));
                filteredGraphicNovels.add(filteredGraphicNovel);
            }
            globalFilteredObject.setFilteredGraphicNovels(filteredGraphicNovels);
        }
        // Too much results
        if(list != null && list.is("p")) {
            globalFilteredObject.setFilteredGraphicNovelsMessage(list.text().trim());
        }
    }

}
