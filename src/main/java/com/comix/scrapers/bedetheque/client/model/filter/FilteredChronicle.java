package com.comix.scrapers.bedetheque.client.model.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class FilteredChronicle {

    private String name;
    private String ratingLabel;
    private String ratingUrl;
    private String url;
}
