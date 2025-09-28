package com.comix.scrapers.bedetheque.client.model.statistics;

import lombok.*;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(exclude = {"news", "lastEntries"})
@ToString(exclude = {"news", "lastEntries"})
@NoArgsConstructor
@AllArgsConstructor
public class GlobalStatistics {

    private Integer series;
    private Integer graphicNovels;
    private Integer authors;
    private Integer reviews;
    private List<LastEntry> news;
    private List<LastEntry> lastEntries;
}
