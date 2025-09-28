package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.client.model.filter.GlobalFilteredObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilterGlobalScraperTest {

    private FilterGlobalScraper filterGlobalScraper;

    @BeforeEach
    void setUp() {
        filterGlobalScraper = new FilterGlobalScraper();
        // On injecte manuellement les valeurs de configuration pour le test
        filterGlobalScraper.setBedethequeGlobalSearchUrl("https://test.com/search");
        filterGlobalScraper.setLatency(0L);
        filterGlobalScraper.setLocalCacheActive(false);
    }

    @Test
    @DisplayName("doit parser correctement une page avec plusieurs types de résultats")
    void filter_shouldParsePageWithMultipleResultTypes() {
        // GIVEN: Un document HTML simulé avec des auteurs, des séries et des albums
        String html = """
            <div>
                <div class="search-line">
                    <h3>1 auteur trouvé pour "test"</h3>
                </div>
                <div class="clear"></div>
                <ul>
                    <li><a href="https://test.com/auteur-1-Test.html"><span>Test, Auteur</span></a><span class="count">(France)</span></li>
                </ul>
                <div class="search-line">
                    <h3>1 série trouvée pour "test"</h3>
                </div>
                <div class="clear"></div>
                <ul>
                    <li><span class="ico"><img src="flag.png" /></span><a href="https://test.com/serie-2-Test.html"><span>Test Serie</span></a><span class="count">Catégorie</span></li>
                </ul>
                <div class="search-line">
                    <h3>1 album trouvé pour "test"</h3>
                </div>
                <div class="clear"></div>
                <ul>
                    <li><span class="ico"><img src="flag_album.png" /></span><a href="https://test.com/album-3-Test.html"><span>Test Album</span></a><span class="count">01/2024</span></li>
                </ul>
            </div>
            """;
        Document doc = Jsoup.parse(html);

        // On mock le singleton pour qu'il ne fasse pas d'appel réseau
        try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class)) {
            GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
            mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);
            when(mockScraper.load(any(String.class), any(Map.class), anyLong())).thenReturn(doc);

            // WHEN: On appelle la méthode à tester
            GlobalFilteredObject result = filterGlobalScraper.filter("test");

            // THEN: On vérifie que les données ont été correctement extraites et réparties
            assertThat(result).isNotNull();
            assertThat(result.getFilter()).isEqualTo("test");

            // Auteurs
            assertThat(result.getFilteredAuthors()).hasSize(1);
            assertThat(result.getFilteredAuthors().getFirst().getName()).isEqualTo("Test, Auteur");
            assertThat(result.getFilteredAuthors().getFirst().getUrl()).isEqualTo("https://test.com/auteur-1-Test.html");
            assertThat(result.getFilteredAuthors().getFirst().getNationality()).isEqualTo("(France)");

            // Séries
            assertThat(result.getFilteredSeries()).hasSize(1);
            assertThat(result.getFilteredSeries().getFirst().getTitle()).isEqualTo("Test Serie");
            assertThat(result.getFilteredSeries().getFirst().getUrl()).isEqualTo("https://test.com/serie-2-Test.html");
            assertThat(result.getFilteredSeries().getFirst().getCategory()).isEqualTo("Catégorie");
            assertThat(result.getFilteredSeries().getFirst().getFlagUrl()).isEqualTo("flag.png");

            // Albums
            assertThat(result.getFilteredGraphicNovels()).hasSize(1);
            assertThat(result.getFilteredGraphicNovels().getFirst().getTitle()).isEqualTo("Test Album");
            assertThat(result.getFilteredGraphicNovels().getFirst().getPublicationDate()).isEqualTo("01/2024");

            // Les autres listes doivent être vides
            assertThat(result.getFilteredChronicles()).isEmpty();
            assertThat(result.getFilteredNews()).isEmpty();
            assertThat(result.getFilteredPreviews()).isEmpty();
            assertThat(result.getFilteredAssociateSeries()).isEmpty();
        }
    }

    @Test
    @DisplayName("doit gérer le message 'Trop de résultats' pour une catégorie")
    void filter_shouldHandleTooManyResultsMessage() {
        // GIVEN: Un document HTML avec un message d'erreur au lieu d'une liste
        String html = """
            <div>
                <div class="search-line">
                    <h3>Trop de séries trouvées pour "a"</h3>
                </div>
                <div class="clear"></div>
                <p>Trop de résultats, veuillez affiner votre recherche.</p>
            </div>
            """;
        Document doc = Jsoup.parse(html);

        try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class)) {
            GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
            mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);
            when(mockScraper.load(any(String.class), any(Map.class), anyLong())).thenReturn(doc);

            // WHEN
            GlobalFilteredObject result = filterGlobalScraper.filter("a");

            // THEN
            assertThat(result.getFilteredSeries()).isNotNull().isEmpty();
            assertThat(result.getFilteredSeriesMessage()).isEqualTo("Trop de résultats, veuillez affiner votre recherche.");
        }
    }

    @Test
    @DisplayName("doit retourner un objet vide si la page de résultats est vide")
    void filter_shouldReturnEmptyObject_whenResultPageIsEmpty() {
        // GIVEN: Un document HTML sans aucun 'div.search-line'
        String html = "<div>Aucun résultat</div>";
        Document doc = Jsoup.parse(html);

        try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class)) {
            GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
            mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);
            when(mockScraper.load(any(String.class), any(Map.class), anyLong())).thenReturn(doc);

            // WHEN
            GlobalFilteredObject result = filterGlobalScraper.filter("recherche-vide");

            // THEN
            assertThat(result.getFilteredAuthors()).isEmpty();
            assertThat(result.getFilteredSeries()).isEmpty();
            assertThat(result.getFilteredGraphicNovels()).isEmpty();
            assertThat(result.getFilteredChronicles()).isEmpty();
            assertThat(result.getFilteredNews()).isEmpty();
            assertThat(result.getFilteredPreviews()).isEmpty();
            assertThat(result.getFilteredAssociateSeries()).isEmpty();
        }
    }

    @Test
    @DisplayName("doit correctement parser les séries associées (mot clé)")
    void filter_shouldParseAssociateSeriesCorrectly() {
        // GIVEN: Un document HTML avec des séries associées
        String html = """
            <div>
                <div class="search-line">
                    <h3>3 séries sur le mot clé <span class="orange">gaston</span> trouvées</h3>
                </div>
                <div class="clear"></div>
                <ul>
                    <li>
                        <span class="ico"><img src="https://www.bdgest.com/skin/flags/France.png"></span>
                        <a href="https://www.bedetheque.com/serie-31-BD-Gaston.html">
                            <span class="libelle"><span class="highlight">Gaston</span></span>
                        </a>
                        <span class="count">Humour</span>
                    </li>
                    <li><span class="ico"><img src="flag_assoc.png" /></span><a href="https://test.com/serie-assoc-1.html"><span>Série Associée 1</span></a><span class="count">Aventure</span></li>
                </ul>
            </div>
            """;
        Document doc = Jsoup.parse(html);

        try (MockedStatic<GenericScraperSingleton> mockedSingleton = Mockito.mockStatic(GenericScraperSingleton.class)) {
            GenericScraperSingleton mockScraper = mock(GenericScraperSingleton.class);
            mockedSingleton.when(GenericScraperSingleton::getInstance).thenReturn(mockScraper);
            when(mockScraper.load(any(String.class), any(Map.class), anyLong())).thenReturn(doc);

            // WHEN
            GlobalFilteredObject result = filterGlobalScraper.filter("gaston");

            // THEN
            assertThat(result.getFilteredSeries()).as("La liste des séries normales doit être vide").isEmpty();
            assertThat(result.getFilteredAssociateSeries()).as("La liste des séries associées doit contenir un élément").hasSize(2);
            assertThat(result.getFilteredAssociateSeries().getFirst().getTitle()).isEqualTo("Gaston");
            assertThat(result.getFilteredAssociateSeries().getFirst().getUrl()).isEqualTo("https://www.bedetheque.com/serie-31-BD-Gaston.html");
        }
    }
}