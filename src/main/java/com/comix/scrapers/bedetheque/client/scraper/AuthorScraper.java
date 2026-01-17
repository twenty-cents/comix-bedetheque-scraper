package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.client.model.author.*;
import com.comix.scrapers.bedetheque.client.model.serie.SerieLanguage;
import com.comix.scrapers.bedetheque.client.model.serie.SerieToDiscover;
import com.comix.scrapers.bedetheque.exception.TechnicalException;
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
import java.util.Optional;

@Slf4j
@Component
public class AuthorScraper extends GenericScraper {

    @Setter
    @Value("${bedetheque.url.authors.index-by-letter}")
    private String bedethequeAuthorsListByLetter;

    @Setter
    @Value("${bedetheque.url.author.prefix}")
    private String bedethequeAuthorPrefixUrl;

    @Value("${application.downloads.authors.photo.hd}")
    private String outputAuthorHdDirectory;

    @Value("${application.http.medias.authors.photo.hd}")
    private String httpAuthorHdPath;

    @Value("${application.downloads.graphic-novels.cover-front.thumbs}")
    private String outputCoverFrontThumbDirectory;

    @Value("${application.http.medias.graphic-novels.cover-front.thumbs}")
    private String httpCoverFrontThumbDirectory;

    @Setter
    @Value("${application.downloads.localcache.active}")
    private boolean isLocalCacheActive;

    @Setter
    @Value("${application.scraping.latency}")
    private long latency;

    /**
     * Extract the author id from an author bedetheque url
     *
     * @param bedethequeAuthorUrl the author bedetheque url
     * @return an author bedetheque id
     */
    public String getBedethequeId(String bedethequeAuthorUrl) {
        if (StringUtils.isBlank(bedethequeAuthorUrl)) {
            return "";
        }
        String[] parts = StringUtils.split(bedethequeAuthorUrl, "-");
        if (parts.length > 2) {
            return parts[1];
        } else {
            log.warn("No Bedetheque author Id found for url {}", bedethequeAuthorUrl);
            return "";
        }
    }

    /**
     * Build an index url with all authors starting with a letter
     *
     * @param letter the letter
     * @return an url with all author links starting with the given letter
     */
    public String getIndexUrlsByLetter(String letter) {
        return bedethequeAuthorsListByLetter.replaceFirst("0", letter);
    }

    /**
     * Get alphabetical indexes of all authors
     *
     * @return a list of author indexes
     */
    public List<AuthorsByLetter> listAllAuthorsIndexes() {
        // Get all existing authors urls from http://www.bedetheque.com
        List<AuthorsByLetter> authorsByLetters = new ArrayList<>();
        String[] letters = "0-A-B-C-D-E-F-G-H-I-J-K-L-M-N-O-P-Q-R-S-T-U-V-W-X-Y-Z".split("-");
        for (String letter : letters) {
            var authorsByLetter = new AuthorsByLetter();
            authorsByLetter.setLetter(letter);
            authorsByLetter.setUrl(getIndexUrlsByLetter(letter));
            authorsByLetters.add(authorsByLetter);
        }

        log.info("Scraped {} lists of indexed authors.", authorsByLetters.size());
        return authorsByLetters;
    }

    /**
     * Scrap all authors starting with a letter <n> from <a href="http://wwww.bedetheque.com">...</a>
     *
     * @param letter the letter from which the author list should be scraped
     * @return a list of scraped author urls
     */
    public List<Author> scrapAuthorsIndexedByLetter(String letter) {
        String authorsIndexedUrl = getIndexUrlsByLetter(letter);
        // Load all authors starting with the letter
        Document doc = GenericScraperSingleton.getInstance().load(authorsIndexedUrl, latency);

        // Retrieve all authors from the html page
        List<Author> authors = new ArrayList<>();
        var links = doc.getElementsByTag("a");
        for (Element link : links) {
            String linkHref = link.attr("href");
            if (linkHref.contains(bedethequeAuthorPrefixUrl)) {
                String id = getBedethequeId(linkHref);
                // Define a new author
                var author = new Author();
                author.setId(id);
                author.setName(link.text());
                author.setUrl(linkHref);
                // Add him to the author list
                authors.add(author);
            }
        }

        log.info("Scraped {} authors whose name starting with {}", authors.size(), letter);
        return authors;
    }

    /**
     * Scrap an author from <a href="http://wwww.bedetheque.com">...</a>
     *
     * @param authorUrl the scraped author url to get
     * @return a generic scraped author from <a href="http://wwww.bedetheque.com">...</a>
     */
    public AuthorDetails scrap(String authorUrl) {
        var bedethequeAuthor = new Author();
        bedethequeAuthor.setUrl(authorUrl);
        return scrap(bedethequeAuthor);
    }

    /**
     * Scrap an author from <a href="http://wwww.bedetheque.com">...</a>
     *
     * @param author the scraped author url to get
     * @return a generic scraped author from <a href="http://wwww.bedetheque.com">...</a>
     */
    public AuthorDetails scrap(Author author) {
        // Load author
        Document doc = GenericScraperSingleton.getInstance().load(author.getUrl(), latency);

        var authorDetails = new AuthorDetails();
        // Scrap author information
        authorDetails.setId(scrap(doc, "ul.auteur-info li:contains(Identifiant)", null));
        authorDetails.setLastname(scrap(doc, "ul.auteur-info li:contains(Nom) > span", null));
        authorDetails.setFirstname(scrap(doc, "ul.auteur-info li:contains(Prénom) > span", null));
        authorDetails.setNickname(scrap(doc, "ul.auteur-info li:contains(Pseudo)", null));
        authorDetails.setBirthdate(scrapAndClean(doc, "ul.auteur-info li:contains(Naissance)", null, "le"));
        authorDetails.setDeceaseDate(scrapAndClean(doc, "ul.auteur-info li:contains(Décès)", null, "le"));
        String nationality = scrap(doc, "ul.auteur-info li:contains(Naissance) > span.pays-auteur", null);
        if (nationality != null) {
            authorDetails.setNationality(nationality
                    .replace('(', ' ')
                    .replace(')', ' ')
                    .trim());
        }
        authorDetails.setSiteUrl(scrap(doc, "ul.auteur-info li:contains(Site) > a", "href"));
        authorDetails.setBiography(scrap(doc, "p.bio", null));
        authorDetails.setOriginalPhotoUrl(scrap(doc, "div.auteur-image > a", "href"));
        authorDetails.setAuthorUrl(author.getUrl());
        // Other pseudonym (to be refactored if more than one is possible)
        String otherPseudonymName = scrap(doc, "ul.auteur-info li:contains(Voir) > a", null);
        String otherPseudonymUrl = scrap(doc, "ul.auteur-info li:contains(Voir) > a", "href");
        String otherPseudonymId = getBedethequeId(otherPseudonymUrl);
        if (!StringUtils.isBlank(otherPseudonymId)) {
            Author otherAuthorPseudonym = new Author();
            otherAuthorPseudonym.setId(otherPseudonymId);
            otherAuthorPseudonym.setName(otherPseudonymName);
            otherAuthorPseudonym.setUrl(otherPseudonymUrl);
            authorDetails.setOtherAuthorPseudonym(otherAuthorPseudonym);
        }
        authorDetails.setAuthorsToDiscover(scrapAuthorsToDiscover(doc));
        authorDetails.setSeriesToDiscover(scrapSerieToDiscover(doc));
        authorDetails.setBibliography(scrapBibliography(doc));

        Optional<Author> optionalPreviousAuthor = scrapPreviousAuthor(doc);
        optionalPreviousAuthor.ifPresent(authorDetails::setPreviousAuthor);

        Optional<Author> optionalNextAuthor = scrapNextAuthor(doc);
        optionalNextAuthor.ifPresent(authorDetails::setNextAuthor);

        // Generic author
        if (authorDetails.getId() == null) {
            String[] urlSplit = author.getUrl().split("-");
            if (urlSplit.length >= 2) {
                authorDetails.setId(author.getUrl().split("-")[1]);
            }
            authorDetails.setNickname(author.getName());

            if (urlSplit.length >= 4) {
                String lastname = urlSplit[3];
                lastname = Strings.CS.remove(lastname, ".html");
                authorDetails.setLastname(lastname);
            }
        }

        if (!StringUtils.isBlank(authorDetails.getOriginalPhotoUrl())) {
            authorDetails.setPhotoFilename(getMediaFilename(authorDetails.getOriginalPhotoUrl()));
            authorDetails.setPhotoUrl(getHashedOutputMediaUrl(authorDetails.getOriginalPhotoUrl(), httpAuthorHdPath, authorDetails.getId()));
            authorDetails.setPhotoPath(getHashedOutputMediaPath(authorDetails.getOriginalPhotoUrl(), outputAuthorHdDirectory, author.getId()));
            authorDetails.setIsPhotoUrlChecked(false);
            authorDetails.setPhotoFileSize(0L);
        }

        // Download all thumbs in the local server
        if (isLocalCacheActive) {
            downloadPhoto(authorDetails);
            downloadSerieCovers(authorDetails);
        }
        log.info("Scraped author : {} {}, {}",
                authorDetails.getId(),
                authorDetails.getLastname(),
                authorDetails.getFirstname());
        return authorDetails;
    }

    /**
     * Scrap the previous author url
     *
     * @param doc the document to scrap
     * @return the previous author url
     */
    private Optional<Author> scrapPreviousAuthor(Document doc) {
        Optional<Author> optionalBedethequeAuthorUrl = Optional.empty();
        Elements as = doc.select("h1.single-title-auteur a");
        for (Element a : as) {
            String rel = a.attr("rel");
            String id = getBedethequeId(a.attr("href"));
            if (rel.equals("prev")) {
                var author = new Author();
                author.setId(id);
                author.setName(a.attr(HTML.Attribute.TITLE.toString()));
                author.setUrl(a.attr("href"));
                optionalBedethequeAuthorUrl = Optional.of(author);
            }
        }
        return optionalBedethequeAuthorUrl;
    }

    /**
     * Scrap the next author url
     *
     * @param doc the document to scrap
     * @return the next author url
     */
    private Optional<Author> scrapNextAuthor(Document doc) {
        Optional<Author> optionalBedethequeAuthorUrl = Optional.empty();
        Elements as = doc.select("h1.single-title-auteur a");
        for (Element a : as) {
            String rel = a.attr("rel");
            String id = getBedethequeId(a.attr("href"));
            if (rel.equals("next")) {
                var author = new Author();
                author.setId(id);
                author.setName(a.attr(HTML.Attribute.TITLE.toString()));
                author.setUrl(a.attr("href"));
                optionalBedethequeAuthorUrl = Optional.of(author);
            }
        }
        return optionalBedethequeAuthorUrl;
    }

    /**
     * Scrap the list of all authors to discover
     *
     * @param doc the document to scrap
     * @return the list of all authors to discover
     */
    private List<Author> scrapAuthorsToDiscover(Document doc) {
        List<Author> authorsToDiscover = new ArrayList<>();
        Elements lis = doc.select("div.serie-liee li");
        for (Element li : lis) {
            String name = attr(li.selectFirst("a"), HTML.Attribute.TITLE);
            String url = attr(li.selectFirst("a"), HTML.Attribute.HREF);
            String id = getBedethequeId(url);
            Author b = new Author();
            b.setId(id);
            b.setName(name);
            b.setUrl(url);
            authorsToDiscover.add(b);
        }
        Collections.sort(authorsToDiscover);
        return authorsToDiscover;
    }

    /**
     * Scrap the list of all associate series to discover
     *
     * @param doc the document to scrap
     * @return the list of all associate series to discover
     */
    private List<SerieToDiscover> scrapSerieToDiscover(Document doc) {
        List<SerieToDiscover> seriesToDiscover = new ArrayList<>();
        Elements as = doc.select("div.tab_content ul.gallery-side a");
        for (Element a : as) {
            Element img = a.selectFirst("img");
            Element e = a.parent();
            String title = null;
            if (e != null) {
                title = ownText(e.selectFirst("span"));
            }
            SerieToDiscover serie = new SerieToDiscover();
            serie.setUrl(a.attr("href"));
            serie.setTitle(title);
            serie.setId(this.getIdBel(a.attr("href")));
            if (img != null) {
                serie.setOriginalCoverUrl(img.attr("src"));
                serie.setCoverFilename(getMediaFilename(serie.getOriginalCoverUrl()));
                serie.setCoverUrl(getHashedOutputMediaUrl(serie.getOriginalCoverUrl(), httpCoverFrontThumbDirectory, serie.getId()));
                serie.setCoverPath(getHashedOutputMediaUrl(serie.getOriginalCoverUrl(), outputCoverFrontThumbDirectory, serie.getId()));
                serie.setIsCoverChecked(false);
                serie.setCoverFileSize(0L);
            }
            serie.setCoverTitle(title);

            seriesToDiscover.add(serie);
        }
        return seriesToDiscover;
    }

    /**
     * Get the whole bibliography of an author
     *
     * @param doc the document to scrap
     * @return the whole bibliography of an author
     */
    private List<Collaboration> scrapBibliography(Document doc) {
        List<Collaboration> collaborations = new ArrayList<>();
        Elements tables = doc.select("table.biblio-auteur");
        for (Element table : tables) {
            Element th = table.selectFirst("th");
            String type = null;
            if (th != null) {
                type = th.ownText();
            }

            Elements trs = table.select("tbody tr");
            List<CollaborationDetails> bibliographies = new ArrayList<>();
            for (Element tr : trs) {
                Elements tds = tr.select("td");
                String title = ownText(tds.getFirst().selectFirst("a"));
                String serieUrl = attr(tds.getFirst().selectFirst("a"), HTML.Attribute.HREF);
                String serieId = getBedethequeId(serieUrl);
                var serieLanguage = SerieLanguage.fromFlag(attr(tds.get(0).selectFirst("img"), HTML.Attribute.SRC));
                String fromYear = ownText(tds.get(1).selectFirst("span"));
                String toYear = ownText(tds.get(2).selectFirst("span"));

                // Roles
                List<String> roles = new ArrayList<>();
                Elements is = tds.get(3).select("i");
                for (Element i : is) {
                    roles.add(i.attr("title"));
                }
                Element span = tds.get(3).selectFirst("span");
                if (span != null) {
                    String role = span.ownText();
                    if (!role.trim().isEmpty()) {
                        roles.add(role);
                    }
                }
                var collaborationDetails = new CollaborationDetails();
                collaborationDetails.setId(serieId);
                collaborationDetails.setTitle(title);
                collaborationDetails.setSerieUrl(serieUrl);
                collaborationDetails.setLanguage(serieLanguage);
                collaborationDetails.setFromYear(fromYear);
                collaborationDetails.setToYear(toYear);
                collaborationDetails.setRoles(roles);
                bibliographies.add(collaborationDetails);
            }
            var collaboration = new Collaboration();
            collaboration.setType(type);
            collaboration.setCollaborationDetails(bibliographies);
            collaborations.add(collaboration);
        }
        return collaborations;
    }

    /**
     * Scrap an unique value from the DOM
     *
     * @param doc       the document to scrap
     * @param query     the query to apply to extract values
     * @param attribute optional parameter. If not null, search the attribute in the given query to extract values
     * @return a scraped value
     */
    private String scrap(Document doc, String query, String attribute) {
        String res = null;
        var element = doc.selectFirst(query);
        if (element != null) {
            if (attribute == null) {
                res = element.ownText().trim();
            } else {
                res = element.attr(attribute);
            }
        }
        return res;
    }

    /**
     * Scrap an unique value from the DOM
     *
     * @param doc          the document to scrap
     * @param query        the query to apply to extract values
     * @param attribute    optional parameter. If not null, search the attribute in the given query to extract values
     * @param textToRemove the text to remove from the extract value
     * @return a scraped value
     */
    private String scrapAndClean(Document doc, String query, String attribute, String textToRemove) {
        String preprocessedText = scrap(doc, query, attribute);
        if (preprocessedText != null) {
            preprocessedText = preprocessedText.replaceAll(textToRemove, "").trim();
        }
        return preprocessedText;
    }

    /**
     * Get the id BEL from a serie url
     *
     * @param url the serie url
     * @return the id BEL
     */
    private String getIdBel(String url) {
        String res = null;
        String[] urlSplit = url.split("-");
        if (urlSplit.length > 2) {
            res = urlSplit[1];
        }
        return res;
    }

    /**
     * Download author's photo HD in the NFS server
     *
     * @param author the author
     */
    void downloadPhoto(AuthorDetails author) {
        if (!StringUtils.isBlank(author.getOriginalPhotoUrl())) {
            try {
                download(author.getOriginalPhotoUrl(), author.getPhotoPath());
                author.setIsPhotoUrlChecked(true);
                author.setPhotoFileSize(getMediaSize(author.getPhotoPath()));
            } catch (TechnicalException e) {
                author.setIsPhotoUrlChecked(false);
                author.setPhotoFileSize(0L);
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Download author's series to discover (thumbnails) in the NFS server
     *
     * @param author the author
     */
    void downloadSerieCovers(AuthorDetails author) {
        for (SerieToDiscover s : author.getSeriesToDiscover()) {
            if (!StringUtils.isBlank(s.getOriginalCoverUrl())) {
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
}
