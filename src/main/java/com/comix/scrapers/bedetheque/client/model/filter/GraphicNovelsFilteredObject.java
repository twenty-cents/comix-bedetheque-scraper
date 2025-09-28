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
public class GraphicNovelsFilteredObject {

    private String filter;
    private List<FilteredGraphicNovelDetails> filteredGraphicNovelDetails = new ArrayList<>();
    private String filteredGraphicNovelsMessage = "";
}
