package com.comix.scrapers.bedetheque.client.model.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class FilteredPreview {

    private String title;
    private String pagesCount;
    private String url;
}
