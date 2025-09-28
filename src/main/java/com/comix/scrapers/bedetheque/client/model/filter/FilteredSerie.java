package com.comix.scrapers.bedetheque.client.model.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class FilteredSerie {

    private String flagUrl;
    private String title;
    private String category;
    private String url;

    private Integer countLcsd;
}
