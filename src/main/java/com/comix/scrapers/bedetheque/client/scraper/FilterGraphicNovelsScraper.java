package com.comix.scrapers.bedetheque.client.scraper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.comix.scrapers.bedetheque.client.model.filter.AutocompleteSearch;
import com.comix.scrapers.bedetheque.client.model.filter.FilteredGraphicNovelDetails;
import com.comix.scrapers.bedetheque.client.model.filter.GraphicNovelsFilteredObject;
import com.comix.scrapers.bedetheque.client.model.filter.GraphicNovelsFilters;
import com.comix.scrapers.bedetheque.exception.BusinessException;
import com.comix.scrapers.bedetheque.exception.TechnicalException;
import com.comix.scrapers.bedetheque.util.HTML;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Slf4j
@Component
public class FilterGraphicNovelsScraper extends Scraper {

    @Value("${bedetheque.url.search.graphic-novels}")
    private String bedethequeGraphicNovelsSearchUrl;

    @Value("${bedetheque.url.search.categories}")
    private String bedethequeCategoriesSearchUrl;

    @Value("${bedetheque.url.search.series}")
    private String bedethequeSeriesSearchUrl;

    @Value("${bedetheque.url.search.publishers}")
    private String bedethequePublishersSearchUrl;

    @Value("${bedetheque.url.search.collections}")
    private String bedethequeCollectionsSearchUrl;

    @Value("${bedetheque.url.search.authors}")
    private String bedethequeAuthorsSearchUrl;

    @Value("${application.downloads.localcache.active}")
    private boolean isLocalCacheActive;

    @Value("#{new Long('${application.scraping.latency}')}")
    private Long latency;

    /**
     * Find graphic novels by graphic novels search filter
     * @param graphicNovelsFilters the filter
     * @return the filtered results
     */
    public GraphicNovelsFilteredObject filter(GraphicNovelsFilters graphicNovelsFilters) {
        var graphicNovelsFilteredObject = new GraphicNovelsFilteredObject();

        // Get the page
        Map<String, String> data = new HashMap<>();
        data.put("RechIdSerie", graphicNovelsFilters.getSerieId());
        data.put("RechIdAuteur", graphicNovelsFilters.getAuthorId());
        data.put("RechSerie", graphicNovelsFilters.getSerieTitle());
        data.put("RechTitre", graphicNovelsFilters.getGraphicnovelTitle());
        data.put("RechEditeur", graphicNovelsFilters.getPublisher());
        data.put("RechCollection", graphicNovelsFilters.getCollection());
        data.put("RechStyle", graphicNovelsFilters.getCategory());
        data.put("RechAuteur", graphicNovelsFilters.getAuthor());
        data.put("RechISBN", graphicNovelsFilters.getIsbn());
        data.put("RechParution", graphicNovelsFilters.getStatus().toString());
        data.put("RechOrigine", graphicNovelsFilters.getOrigin().toString());
        data.put("RechLangue", graphicNovelsFilters.getLanguage().toString());
        data.put("RechMotCle", graphicNovelsFilters.getKeyword());
        data.put("RechDLDeb", graphicNovelsFilters.getPublicationDateFrom());
        data.put("RechDLFin", graphicNovelsFilters.getPublicationDateTo());
        data.put("RechCoteMin", graphicNovelsFilters.getQuotationMin());
        data.put("RechCoteMax", graphicNovelsFilters.getQuotationMax());
        data.put("RechEO", graphicNovelsFilters.getOriginalEdition());
        Document doc = GenericScraperSingleton.getInstance().load(bedethequeGraphicNovelsSearchUrl, data, latency);

        // Extract results
        Element e = doc.selectFirst("div.widget-line-title");
        if(e != null) {
            getGraphicNovels(e, graphicNovelsFilteredObject);
        }

        // TODO : +5000 graphic novels

        log.info("Scraped {} graphic novels.", graphicNovelsFilteredObject.getFilteredGraphicNovelDetails().size());
        return graphicNovelsFilteredObject;
    }

    /**
     * Get autocomplete on series titles
     * @param term the filter
     * @return a JSON String {id: "62375", label: "Gast", value: "Gast", desc: "skin/flags/France.png"}
     */
    public List<AutocompleteSearch> autocompleteSeries(String term) {
        return autocomplete(bedethequeSeriesSearchUrl, term);
    }

    /**
     * Get autocomplete on series publishers
     * @param term the filter
     * @return a JSON String {id: "Dupond", label: "Dupond", value: "Dupond"}
     */
    public List<AutocompleteSearch> autocompletePublishers(String term) {
        return autocomplete(bedethequePublishersSearchUrl, term);
    }

    /**
     * Get autocomplete on graphic novels collections
     * @param term the filter
     * @return a JSON String {id: "Dupond", label: "Dupond", value: "Dupond"}
     */
    public List<AutocompleteSearch> autocompleteCollections(String term) {
        return autocomplete(bedethequeCollectionsSearchUrl, term);
    }

    /**
     * Get autocomplete on serie categories
     * @param term the filter
     * @return a JSON String {'id'='' , 'label'='', 'value'=''}
     */
    public List<AutocompleteSearch> autocompleteCategories(String term) {
        return autocomplete(bedethequeCategoriesSearchUrl, term);
    }

    /**
     * Get autocomplete on author names
     * @param term the filter
     * @return a JSON String {'id'='' , 'label'='', 'value'=''}
     */
    public List<AutocompleteSearch> autocompleteAuthors(String term) {
        return autocomplete(bedethequeAuthorsSearchUrl, term);
    }

    /**
     * Get graphic novels
     * @param title the title tag
     */
    private void getGraphicNovels(Element title, GraphicNovelsFilteredObject graphicNovelsFilteredObject) {
        List<FilteredGraphicNovelDetails> filteredGraphicNovelDetails = new ArrayList<>();
        // Second sibling => ul or p if nothing or too many elements found
        var ulElement = title.nextElementSibling();
        // Add all graphic novels
        if(ulElement != null && ulElement.is("ul")) {
            List<Element> liElements = ulElement.select("li");
            for(Element e : liElements) {
                // Add a graphic novel in the result list
                FilteredGraphicNovelDetails filteredGraphicNovelDetail = new FilteredGraphicNovelDetails();
                filteredGraphicNovelDetail.setFlagUrl(attr(e.selectFirst("span.ico > img"), HTML.Attribute.SRC));
                filteredGraphicNovelDetail.setSerieTitle(text(e.selectFirst("span.serie")));
                filteredGraphicNovelDetail.setTome(text(e.selectFirst("span.num")));
                filteredGraphicNovelDetail.setNumEdition(text(e.selectFirst("span.numa")));
                filteredGraphicNovelDetail.setTitle(text(e.selectFirst("span.titre")));
                filteredGraphicNovelDetail.setPublicationDate(text(e.selectFirst("span.dl")));
                filteredGraphicNovelDetail.setUrl(attr(e.selectFirst("a"), HTML.Attribute.HREF));
                filteredGraphicNovelDetail.setCoverUrl(attr(e.selectFirst("a"), HTML.Attribute.REL));
                filteredGraphicNovelDetails.add(filteredGraphicNovelDetail);
            }
            graphicNovelsFilteredObject.setFilteredGraphicNovelDetails(filteredGraphicNovelDetails);
        }

        if(ulElement != null && ulElement.is("span.erreur")) {
            graphicNovelsFilteredObject.setFilteredGraphicNovelsMessage(ulElement.text().trim());
        }
    }

    private List<AutocompleteSearch> autocomplete(String url, String term) {
        List<AutocompleteSearch> autocompleteSearches = new ArrayList<>();
        // Get the page
        Connection.Response response;
        try {
            response = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.106 Safari/537.36")
                    .referrer(bedethequeGraphicNovelsSearchUrl)
                    .method(Connection.Method.GET)
                    .header("accept", "application/json, text/javascript, */*; q=0.01")
                    .header("connection", "keep-alive")
                    .data("term", term)
                    .execute();
        } catch (IOException e) {
            throw new TechnicalException("ERR-SCR-007", e, new Object[]{url, term});
        }

        if(response.statusCode() != 200) {
            throw new BusinessException("ERR-SCR-007", new Object[]{url, term});
        }

        String autocomplete = (response.body().equals("riendutout") ? "" : response.body());

        if(autocomplete.compareTo("") > 0) {
            var mapper = new ObjectMapper();
            try {
                autocompleteSearches =  mapper.readValue(autocomplete, new TypeReference<>() {
                });
            } catch (IOException e) {
                throw new TechnicalException("ERR-SCR-008", e, new Object[]{url, term});
            }
        }
        return autocompleteSearches;
    }
}
