package com.comix.scrapers.bedetheque.client.model.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class GlobalFilteredObject {

    private String filter;
    private String globalErrorMessage = "";
    private List<FilteredChronicle> filteredChronicles = new ArrayList<>();
    private String filteredChroniclesMessage = "";
    private List<FilteredNews> filteredNews = new ArrayList<>();
    private String filteredNewsMessage = "";
    private List<FilteredPreview> filteredPreviews = new ArrayList<>();
    private String filteredPreviewsMessage = "";
    private List<FilteredAuthor> filteredAuthors = new ArrayList<>();
    private String filteredAuthorsMessage = "";
    private List<FilteredSerie> filteredSeries = new ArrayList<>();
    private String filteredSeriesMessage = "";
    private List<FilteredSerie> filteredAssociateSeries = new ArrayList<>();
    private String filteredAssociateSeriesMessage = "";
    private List<FilteredGraphicNovel> filteredGraphicNovels = new ArrayList<>();
    private String filteredGraphicNovelsMessage = "";

}
