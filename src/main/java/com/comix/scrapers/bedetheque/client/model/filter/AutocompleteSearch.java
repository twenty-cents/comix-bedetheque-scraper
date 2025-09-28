package com.comix.scrapers.bedetheque.client.model.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class AutocompleteSearch {

    private String id;
    private String label;
    private String value;
    private String desc;
}
